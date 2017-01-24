/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MailWriter extends Shell {
	private Text toText;
	private Text subjectText;
	private Text bodyText;
	EmailStruct email;
	DHEncryptionIO io;
	private Table tblAttachments;

	/**
	 * Launch the application.
	 * @param args
	 */
	//Jacob Burkamper
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			MailWriter shell = new MailWriter(display);
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
	public MailWriter(Display display) {
		super(display, SWT.SHELL_TRIM);
		createContents();
		this.setLayout(new FormLayout());
		
		Composite composite = new Composite(this, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0);
		fd_composite.right = new FormAttachment(100, -16);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new GridLayout(2, false));
		
		Label lblTo = new Label(composite, SWT.NONE);
		GridData gd_lblTo = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblTo.widthHint = 38;
		lblTo.setLayoutData(gd_lblTo);
		lblTo.setText("To:");
		
		toText = new Text(composite, SWT.BORDER);
		toText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblSubject = new Label(composite, SWT.NONE);
		lblSubject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSubject.setText("Subject:");
		
		subjectText = new Text(composite, SWT.BORDER);
		subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_1 = new Composite(this, SWT.NONE);
		fd_composite.bottom = new FormAttachment(composite_1, 0);
		composite_1.setLayout(new FormLayout());
		FormData fd_composite_1 = new FormData();
		fd_composite_1.top = new FormAttachment(composite, 6);
		fd_composite_1.left = new FormAttachment(composite, 0, SWT.LEFT);
		fd_composite_1.right = new FormAttachment(100, -10);
		composite_1.setLayoutData(fd_composite_1);
		
		Label lblMailBody = new Label(composite_1, SWT.NONE);
		FormData fd_lblMailBody = new FormData();
		fd_lblMailBody.right = new FormAttachment(100, 0);
		fd_lblMailBody.left = new FormAttachment(0, 10);
		fd_lblMailBody.top = new FormAttachment(0, 10);
		lblMailBody.setLayoutData(fd_lblMailBody);
		lblMailBody.setText("Mail Body:");
		
		bodyText = new Text(composite_1, SWT.BORDER | SWT.WRAP);
		FormData fd_bodyText = new FormData();
		fd_bodyText.top = new FormAttachment(lblMailBody, 6);
		fd_bodyText.left = new FormAttachment(lblMailBody, 0, SWT.LEFT);
		fd_bodyText.bottom = new FormAttachment(100, -89);
		fd_bodyText.right = new FormAttachment(100, -10);
		bodyText.setLayoutData(fd_bodyText);
		
		tblAttachments = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		tblAttachments.setEnabled(false);
		FormData fd_tblAttachments = new FormData();
		fd_tblAttachments.top = new FormAttachment(bodyText, 6);
		fd_tblAttachments.bottom = new FormAttachment(100, -6);
		fd_tblAttachments.left = new FormAttachment(lblMailBody, 0, SWT.LEFT);
		fd_tblAttachments.right = new FormAttachment(100, -10);
		tblAttachments.setLayoutData(fd_tblAttachments);
		tblAttachments.setHeaderVisible(true);
		tblAttachments.setLinesVisible(true);
		
		TableColumn tblclmnFilename = new TableColumn(tblAttachments, SWT.NONE);
		tblclmnFilename.setWidth(315);
		tblclmnFilename.setText("FileName");
		
		TableColumn tblclmnSize = new TableColumn(tblAttachments, SWT.NONE);
		tblclmnSize.setWidth(184);
		tblclmnSize.setText("Size");
		FormData fd_btnAddAttachment = new FormData();
		fd_btnAddAttachment.left = new FormAttachment(0, 10);
		fd_btnAddAttachment.bottom = new FormAttachment(100, -10);
		
		
		Button btnAddAttachment = new Button(this, SWT.NONE);
		btnAddAttachment.setEnabled(false);
		btnAddAttachment.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showOpenDialog(null);
				if (retVal == JFileChooser.APPROVE_OPTION)
				{
					File newAttachment = fc.getSelectedFile();
					createAttachmentItem(newAttachment);
				}
			}
		});
		fd_composite_1.bottom = new FormAttachment(btnAddAttachment, -6);
		btnAddAttachment.setLayoutData(fd_btnAddAttachment);
		btnAddAttachment.setText("Add Attachment");
		
		
		Button btnCancel = new Button(this, SWT.NONE);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MailWriter.this.close();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(btnAddAttachment, 0, SWT.BOTTOM);
		fd_btnCancel.right = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		Button btnSend = new Button(this, SWT.NONE);
		btnSend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				// Josh Clark
				loadToEmailStruct();
				if (!promptAndEncryptEmail())
					return; // user pressed cancel on password dialog. Don't send email.
				if(checkValidEmailInput(email)){
					writeEmailtoServer();
					MailWriter.this.close();
				}
				else{
					showEmailInputFailureMessage();
				}
			}
		});
		FormData fd_btnSend = new FormData();
		fd_btnSend.top = new FormAttachment(btnAddAttachment, 0, SWT.TOP);
		fd_btnSend.right = new FormAttachment(btnCancel, -6);
		btnSend.setLayoutData(fd_btnSend);
		btnSend.setText("Send");
		
		Menu menu = new Menu(this, SWT.BAR);
		this.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmOpenDraft = new MenuItem(menu_1, SWT.NONE);
		mntmOpenDraft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showOpenDialog(null);
				if (retVal == JFileChooser.APPROVE_OPTION)
				{
					File emailFile = fc.getSelectedFile();
					email = new EmailStruct(emailFile);
					if (email.isEncrypted())
					{
						PasswordDialog pd = new PasswordDialog(MailWriter.this, SWT.PRIMARY_MODAL);
						if(pd.open())
							email.decrypt(pd.getText());
						else // the user cancelled
						{
							email = null;
						}
					}
					updateFields();
				}
				else
				{
					System.out.println("User cancelled draft open.");
				}
			}
		});
		mntmOpenDraft.setText("Open Draft");
		
		MenuItem mntmSaveAsDraft = new MenuItem(menu_1, SWT.NONE);
		mntmSaveAsDraft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadToEmailStruct();
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showSaveDialog(null);
				if (retVal == JFileChooser.APPROVE_OPTION)
				{
					File emailFile = fc.getSelectedFile();
					email.writeToFile(emailFile);
				}
				else
				{
					System.out.println("User cancelled draft save.");
				}
			}
		});
		mntmSaveAsDraft.setText("Save As Draft");
		
		MenuItem mntmSaveAsEncrypted = new MenuItem(menu_1, SWT.NONE);
		mntmSaveAsEncrypted.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadToEmailStruct();
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showSaveDialog(null);
				if (retVal == JFileChooser.APPROVE_OPTION)
				{
					File emailFile = fc.getSelectedFile();
					PasswordDialog pd = new PasswordDialog(MailWriter.this, SWT.PRIMARY_MODAL);
					pd.setMessage("Enter password to encrypt this email");
					if (pd.open())
					{
						email.encrypt(pd.getText());
						email.writeToFile(emailFile);
					}
					else
						System.out.println("user cancelled draft save");
					
				}
				else
				{
					System.out.println("User cancelled draft save.");
				}
			}
		});
		mntmSaveAsEncrypted.setText("Save As Encrypted Draft");
		
		MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
		mntmClose.setText("Close");
	}
	
	//Jacob Burkamper
	MailWriter(Display d, DHEncryptionIO serverIO)
	{
		this(d);
		this.io = serverIO;
	}
	
	//Jacob Burkamper
	//constructor for creating the window with default values
	MailWriter(Display d, String to, String subject, String body, DHEncryptionIO serverIO)
	{
		this(d, serverIO);
		if (to != null)
			toText.setText(to);
		subjectText.setText(subject);
		bodyText.setText(body);
	}

	/**
	 * Create contents of the shell.
	 */
	//Jacob Burkamper
	protected void createContents() {
		setText("New Mail");
		setSize(566, 503);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	//Jacob Burkamper
	private void loadToEmailStruct()
	{
		if (email == null)
			email = new EmailStruct();
		//to field
		System.out.println(toText.getText());
		String[] recipients = toText.getText().split(",");
		for (String recipient : recipients)
			email.addRecipient(recipient.trim());
		
		email.setSubject(subjectText.getText());
		email.setBody(bodyText.getText());
		
		for (TableItem i : tblAttachments.getItems())
		{
			File f = (File)i.getData();
			email.addAttachment(f);
		}
	}
	
	//Jacob Burkamper
	private void updateFields()
	{
		if (email != null)
		{
			toText.setText(email.getToString());
			bodyText.setText(email.getBody());
			subjectText.setText(email.getSubject());
			
			LinkedList<File> attachments = email.getAttachmentList();
			if (attachments != null)
				for (File f : attachments)
				{
					createAttachmentItem(f);
				}
		}
	}
	
	
	// Josh Clark
	private String writeEmailtoServer()
	{
		String returnString;
		try {

			//create the appropriate packet
			PacketHeader emailPacketHeader = new PacketHeader();
			emailPacketHeader.setCommand(Command.SEND_EMAIL);
			
			//send the packet
			io.writeObject(emailPacketHeader);
			//io.flush();
			io.writeObject(email);
			
			//Jacob Burkamper
			if (email.hasAttachments())
				sendAttachments();
			else
			{
				io.writeObject(new PacketHeader(Command.END_EMAIL));
			}
			// end Jacob Burkamper
			
			//get the response
			PacketHeader responsePacket = (PacketHeader)io.readObject();

			if (responsePacket.getCommand() != Command.CONNECT_SUCCESS)
				returnString = "Response Packet contained non-success command";
			else
				returnString = "Successfully sent email to server!";
			
		} catch (Exception e)
		{
			returnString = "Exception thrown while trying to send email.\n" + e;
		}
		return returnString;
	}
	
	//Jacob Burkamper
	//send the attachments from the email to the server.
	private void sendAttachments() throws IOException
	{
		System.out.println("Sending Attachments!"); // TODO: Delete debug code
		final int ARRAY_SIZE = 1000; // size of array to send
		
		io.writeObject(new PacketHeader(Command.START_ATTACHMENTS));
		LinkedList<File> attachments = email.getAttachmentList();
		for (File f : attachments)
		{
			PacketHeader ph = new PacketHeader(Command.SEND_ATTACHMENT);
			
			//calculate the number of byte arrays we will need
			long fSize = f.length();
			long numArrays = fSize / ARRAY_SIZE;
			if (fSize % ARRAY_SIZE > 0)
				numArrays++;
			
			//add the number of byte arrays to the packetheader
			ph.setLength(numArrays);
			
			//add the original name of the file to packetheader
			ph.setString(f.getName());
			
			//send the packet header over the network
			io.writeObject(ph);
			
			//begin sending the file
			FileInputStream fis = new FileInputStream(f);
			byte[] bArr = new byte[ARRAY_SIZE];
			int lengthRead = 0;
			while((lengthRead = fis.read(bArr)) != -1) //read until the end of file
			{
				byte[] sendArray = null;
				if (lengthRead < ARRAY_SIZE) // we didn't read a full array, send a truncated copy
					sendArray = Arrays.copyOf(bArr, lengthRead);
				else
					sendArray = bArr; // we did read the full array. java shallow assignment ftw
				io.writeObject(sendArray);
			}
			fis.close(); // done reading the file
		}
		// we've sent all the attachments.
		io.writeObject(new PacketHeader(Command.END_ATTACHMENTS));
	}
	
	// Josh Clark
	private boolean checkValidEmailInput(EmailStruct myEmail){
		if(subjectText.getText().isEmpty() || toText.getText().isEmpty()){
			return false;
		}
		else{
			return true;
		}
	}
	
	// Josh Clark
	private void showEmailInputFailureMessage()
	{
		Shell invalid = new Shell();
		MessageBox messageBox = new MessageBox(invalid, SWT.OK);
		messageBox.setText("Invalid Email");
		if(toText.getText().isEmpty() && !subjectText.getText().isEmpty()){
			messageBox.setMessage("Please provide at least one recipient.");
		}
		else if(!toText.getText().isEmpty() && subjectText.getText().isEmpty()){
			messageBox.setMessage("Please provide a subject line.");
		}
		else {
			messageBox.setMessage("Please provide at least one recipient and a subject line.");
		}
		
		
		messageBox.open();
	}
	
	//Jacob Burkamper
	//Prompts the user about whether they want to encrypt the email or not.
	// If yes, will ask for a password and encrypt the email using that password.
	// if no, does nothing else.
	// returns false on password cancel. Otherwise returns true
	private boolean promptAndEncryptEmail()
	{
		MessageBox promptBox = new MessageBox(this, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.PRIMARY_MODAL);
		promptBox.setText("Message");
		promptBox.setMessage("Do you want to encrypt this email before sending it?");
		int ret = promptBox.open();
		if (ret == SWT.YES)
		{
			PasswordDialog pd = new PasswordDialog(this, SWT.PRIMARY_MODAL);
			pd.setMessage("Input password to use for encryption");
			if (pd.open())
				email.encrypt(pd.getText());
			else // user canceled password input
				return false;
		}
		return true;
	}
	
	//http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	//Jacob Burkamper
	//creates a TableItem with the File and adds it to the attachment table.
	private void createAttachmentItem(File f)
	{
		if (!f.exists())
			System.out.println("Error! No file with that path!");
		TableItem attachmentItem = new TableItem(tblAttachments, SWT.NONE);
		attachmentItem.setData(f);
		attachmentItem.setText(0, f.getName());
		attachmentItem.setText(1, readableFileSize( f.length() ));
	}
}
