/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import java.text.DateFormat;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class EmailReader extends Shell {
	private Text txtTo;
	private Text txtFrom;
	private Text txtDate;
	private Text txtSubject;
	private Table tblAttachments;
	private StyledText stxtBody;
	private DHEncryptionIO io;
	private EmailStruct email;

	/**
	 * Launch the application.
	 * @param args
	 */
	//Jacob Burkamper
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			EmailReader shell = new EmailReader(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 * @wbp.parser.constructor
	 */
	//Jacob Burkamper
	public EmailReader(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new FormLayout());
		
		Label lblTo = new Label(this, SWT.NONE);
		FormData fd_lblTo = new FormData();
		fd_lblTo.left = new FormAttachment(0, 10);
		fd_lblTo.top = new FormAttachment(0, 7);
		lblTo.setLayoutData(fd_lblTo);
		lblTo.setText("To:");
		
		Label lblFrom = new Label(this, SWT.NONE);
		FormData fd_lblFrom = new FormData();
		fd_lblFrom.top = new FormAttachment(lblTo, 10);
		fd_lblFrom.left = new FormAttachment(0, 10);
		fd_lblFrom.right = new FormAttachment(0, 65);
		lblFrom.setLayoutData(fd_lblFrom);
		lblFrom.setText("From:");
		
		Label lblDate = new Label(this, SWT.NONE);
		FormData fd_lblDate = new FormData();
		fd_lblDate.top = new FormAttachment(lblFrom, 10);
		fd_lblDate.left = new FormAttachment(lblTo, 0, SWT.LEFT);
		fd_lblDate.right = new FormAttachment(0, 65);
		lblDate.setLayoutData(fd_lblDate);
		lblDate.setText("Date:");
		
		Label lblSubject = new Label(this, SWT.NONE);
		FormData fd_lblSubject = new FormData();
		fd_lblSubject.top = new FormAttachment(lblDate, 10);
		fd_lblSubject.left = new FormAttachment(lblTo, 0, SWT.LEFT);
		fd_lblSubject.right = new FormAttachment(0, 65);
		lblSubject.setLayoutData(fd_lblSubject);
		lblSubject.setText("Subject:");
		
		Label lblAttachments = new Label(this, SWT.NONE);
		FormData fd_lblAttachments = new FormData();
		fd_lblAttachments.right = new FormAttachment(0, 90);
		fd_lblAttachments.top = new FormAttachment(0, 109);
		fd_lblAttachments.left = new FormAttachment(0, 10);
		lblAttachments.setLayoutData(fd_lblAttachments);
		lblAttachments.setText("Attachments:");
		
		txtTo = new Text(this, SWT.BORDER);
		fd_lblTo.right = new FormAttachment(0,63);
		FormData fd_txtTo = new FormData();
		fd_txtTo.right = new FormAttachment(100, -10);
		fd_txtTo.left = new FormAttachment(lblTo, 6);
		fd_txtTo.top = new FormAttachment(lblTo, -3, SWT.TOP);
		txtTo.setLayoutData(fd_txtTo);
		txtTo.setEditable(false);
		
		txtFrom = new Text(this, SWT.BORDER);
		FormData fd_txtFrom = new FormData();
		fd_txtFrom.left = new FormAttachment(lblFrom, 4);
		fd_txtFrom.right = new FormAttachment(txtTo, 0, SWT.RIGHT);
		fd_txtFrom.top = new FormAttachment(lblFrom, -3, SWT.TOP);
		txtFrom.setLayoutData(fd_txtFrom);
		txtFrom.setEditable(false);
		
		txtDate = new Text(this, SWT.BORDER);
		FormData fd_txtDate = new FormData();
		fd_txtDate.right = new FormAttachment(100, -10);
		fd_txtDate.left = new FormAttachment(lblDate, 4);
		fd_txtDate.top = new FormAttachment(txtFrom, 3);
		txtDate.setLayoutData(fd_txtDate);
		txtDate.setEditable(false);
		
		txtSubject = new Text(this, SWT.BORDER);
		FormData fd_txtSubject = new FormData();
		fd_txtSubject.left = new FormAttachment(lblSubject, 59, SWT.LEFT);
		fd_txtSubject.right = new FormAttachment(txtTo, 0, SWT.RIGHT);
		//fd_txtSubject.bottom = new FormAttachment(lblSubject, 0, SWT.BOTTOM);
		fd_txtSubject.top = new FormAttachment(txtDate, 3);
		txtSubject.setLayoutData(fd_txtSubject);
		txtSubject.setEditable(false);
		
		tblAttachments = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_tblAttachments = new FormData();
		fd_tblAttachments.right = new FormAttachment(txtTo, 0, SWT.RIGHT);
		fd_tblAttachments.top = new FormAttachment(0, 130);
		fd_tblAttachments.left = new FormAttachment(0, 10);
		tblAttachments.setLayoutData(fd_tblAttachments);
		tblAttachments.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				TableItem clickedItem = tblAttachments.getItem(pt);
				if (clickedItem != null)
					handleAttachment((File)clickedItem.getData());
			}
		});
		tblAttachments.setHeaderVisible(true);
		tblAttachments.setLinesVisible(true);
		
		TableColumn tblclmnFileName = new TableColumn(tblAttachments, SWT.NONE);
		tblclmnFileName.setWidth(513);
		tblclmnFileName.setText("File Name");
		
		TableColumn tblclmnFileSize = new TableColumn(tblAttachments, SWT.NONE);
		tblclmnFileSize.setWidth(100);
		tblclmnFileSize.setText("File Size");
		
		Label lblBody = new Label(this, SWT.NONE);
		FormData fd_lblBody = new FormData();
		fd_lblBody.right = new FormAttachment(0, 65);
		fd_lblBody.top = new FormAttachment(0, 181);
		fd_lblBody.left = new FormAttachment(0, 10);
		lblBody.setLayoutData(fd_lblBody);
		lblBody.setText("Body:");
		
		stxtBody = new StyledText(this, SWT.BORDER);
		FormData fd_stxtBody = new FormData();
		fd_stxtBody.top = new FormAttachment(lblBody, 6);
		fd_stxtBody.right = new FormAttachment(txtTo, 0, SWT.RIGHT);
		fd_stxtBody.left = new FormAttachment(0, 10);
		stxtBody.setLayoutData(fd_stxtBody);
		stxtBody.setEditable(false);
		
		Button btnClose = new Button(this, SWT.NONE);
		fd_stxtBody.bottom = new FormAttachment(btnClose, -6);
		FormData fd_btnClose = new FormData();
		fd_btnClose.top = new FormAttachment(100, -30);
		fd_btnClose.right = new FormAttachment(txtTo, 0, SWT.RIGHT);
		fd_btnClose.left = new FormAttachment(100, -80);
		btnClose.setLayoutData(fd_btnClose);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				EmailReader.this.close();
			}
		});
		btnClose.setText("Close");
		
		Button btnReply = new Button(this, SWT.NONE);
		FormData fd_btnReply = new FormData();
		fd_btnReply.top = new FormAttachment(stxtBody, 9);
		fd_btnReply.left = new FormAttachment(lblTo, 0, SWT.LEFT);
		fd_btnReply.right = new FormAttachment(0, 85);
		btnReply.setLayoutData(fd_btnReply);
		btnReply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MailWriter newMail = new MailWriter( Display.getCurrent(), 
							txtFrom.getText(),
							"Re: " + txtSubject.getText(),
							stxtBody.getText(),
							io
						);
				newMail.open();
			}
		});
		btnReply.setText("Reply");
		
		Button btnReplyall = new Button(this, SWT.NONE);
		FormData fd_btnReplyall = new FormData();
		fd_btnReplyall.top = new FormAttachment(btnReply, 0, SWT.TOP);
		fd_btnReplyall.left = new FormAttachment(btnReply, 6);
		btnReplyall.setLayoutData(fd_btnReplyall);
		btnReplyall.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MailWriter newMail = new MailWriter( Display.getCurrent(), 
						txtTo.getText() + "," + txtFrom.getText(),
						"Re: " + txtSubject.getText(),
						stxtBody.getText(),
						io
					);
			newMail.open();
			}
		});
		btnReplyall.setText("Reply-All");
		
		Button btnForward = new Button(this, SWT.NONE);
		fd_btnReplyall.right = new FormAttachment(btnForward, -6);
		FormData fd_btnForward = new FormData();
		fd_btnForward.top = new FormAttachment(stxtBody, 9);
		fd_btnForward.left = new FormAttachment(0, 172);
		fd_btnForward.right = new FormAttachment(0, 247);
		btnForward.setLayoutData(fd_btnForward);
		btnForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MailWriter newMail = new MailWriter( Display.getCurrent(), 
						null,
						"Fwd: " + txtSubject.getText(),
						stxtBody.getText(),
						io
					);
			newMail.open();
			}
		});
		btnForward.setText("Forward");
		createContents();
	}
	
	//Jacob Burkamper
	EmailReader(Display d, DHEncryptionIO serverIO)
	{
		this(d);
		this.io = serverIO;
	}
	
	//Jacob Burkamper
	EmailReader(Display d, EmailStruct email, UserStruct from, Date emailDate, DHEncryptionIO serverIO)
	{
		this(d, serverIO);
		txtTo.setText(email.getToString());
		txtFrom.setText(from.compile());
		txtDate.setText(DateFormat.getDateTimeInstance().format(emailDate));
		txtSubject.setText(email.getSubject());
		
		//handle the attachments
		LinkedList<File> attachments = email.getAttachmentList();
		if(attachments != null){
			for (File f : attachments)
			{
				TableItem t = new TableItem(tblAttachments, 0);
				t.setData(f);
				t.setText(0, f.getName()); // filename
				t.setText(1, String.valueOf(f.length()));
			}		
		}
		
		this.email = email;
	}
	
	//Jacob Burkamper
	@Override
	public void open() 
	{
		if (email.isEncrypted())
		{
			PasswordDialog pd = new PasswordDialog(this, SWT.PRIMARY_MODAL);
			pd.setMessage("The email you are trying to read is encrypted. Please enter the password.");
			if (pd.open())
			{
				email.decrypt(pd.getText());
				stxtBody.setText(email.getBody());
			}
			else
				return;
		}
		stxtBody.setText(email.getBody());
		super.open();		
	}

	/**
	 * Create contents of the shell.
	 */
	//Jacob Burkamper
	protected void createContents() {
		setText("EmailReader");
		setSize(661, 560);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	//Jacob Burkamper
	private void handleAttachment(File f)
	{
		//TODO: Handle the attachment.
		MessageBox messageBox = new MessageBox(this);
		messageBox.setText("Attachment Clicked");
		messageBox.setMessage("Attachment handling currently not implemented.\n\nYou clicked on file: "+f.getName());
		messageBox.open();
	}
}
