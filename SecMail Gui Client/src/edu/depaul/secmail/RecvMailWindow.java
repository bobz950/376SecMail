/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.text.DateFormat;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Label;

public class RecvMailWindow extends Shell {
	private Table tblNewMail;
	private DHEncryptionIO io;
	private Table tblSentMail;

	/**
	 * Launch the application.
	 * @param args
	 */
	//Jacob Burkamper
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			RecvMailWindow shell = new RecvMailWindow(display);
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
	public RecvMailWindow(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new FormLayout());
		
		tblNewMail = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_tblNewMail = new FormData();
		fd_tblNewMail.right = new FormAttachment(100, -10);
		fd_tblNewMail.left = new FormAttachment(0, 10);
		tblNewMail.setLayoutData(fd_tblNewMail);
		tblNewMail.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				TableItem clickedItem = tblNewMail.getItem(pt);
				if (clickedItem != null)
					try {
						OpenOrFetchMail(clickedItem);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		});
		tblNewMail.setHeaderVisible(true);
		tblNewMail.setLinesVisible(true);
		
		TableColumn tblclmnNewColumn = new TableColumn(tblNewMail, SWT.NONE);
		tblclmnNewColumn.setWidth(149);
		tblclmnNewColumn.setText("From");
		
		TableColumn tblclmnSubject = new TableColumn(tblNewMail, SWT.NONE);
		tblclmnSubject.setWidth(273);
		tblclmnSubject.setText("Subject");
		
		TableColumn tblclmnDate = new TableColumn(tblNewMail, SWT.NONE);
		tblclmnDate.setWidth(130);
		tblclmnDate.setText("Date");
		
		TableColumn tblclmnRecieved = new TableColumn(tblNewMail, SWT.NONE);
		tblclmnRecieved.setWidth(63);
		tblclmnRecieved.setText("Opened");
		
		Button btnClose = new Button(this, SWT.NONE);
		fd_tblNewMail.bottom = new FormAttachment(100, -276);
		FormData fd_btnClose = new FormData();
		fd_btnClose.right = new FormAttachment(100, -10);
		fd_btnClose.bottom = new FormAttachment(100, -10);
		btnClose.setLayoutData(fd_btnClose);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				RecvMailWindow.this.close();
			}
		});
		btnClose.setText("Close");
		
		Button btnGetNotifications = new Button(this, SWT.NONE);
		//fd_btnClose.left = new FormAttachment(0, 626);
		FormData fd_btnGetNotifications = new FormData();
		fd_btnGetNotifications.top = new FormAttachment(btnClose, 0, SWT.TOP);
		fd_btnGetNotifications.right = new FormAttachment(btnClose, -6);
		//fd_btnGetNotifications.left = new FormAttachment(btnClose, -60);
		btnGetNotifications.setLayoutData(fd_btnGetNotifications);
		btnGetNotifications.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				getNewNotifications();
			}
		});
		btnGetNotifications.setText("Get Notifications");
		
		Label lblNewMail = new Label(this, SWT.NONE);
		fd_tblNewMail.top = new FormAttachment(lblNewMail, 6);
		FormData fd_lblNewMail = new FormData();
		fd_lblNewMail.right = new FormAttachment(0, 105);
		fd_lblNewMail.left = new FormAttachment(0, 10);
		fd_lblNewMail.top = new FormAttachment(0, 10);
		lblNewMail.setLayoutData(fd_lblNewMail);
		lblNewMail.setText("New Mail");
		
		Label lblSentMail = new Label(this, SWT.NONE);
		FormData fd_lblSentMail = new FormData();
		fd_lblSentMail.top = new FormAttachment(tblNewMail, 6);
		fd_lblSentMail.left = new FormAttachment(tblNewMail, 0, SWT.LEFT);
		lblSentMail.setLayoutData(fd_lblSentMail);
		lblSentMail.setText("Sent Mail Receipts");
		
		tblSentMail = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_tblSentMail = new FormData();
		fd_tblSentMail.bottom = new FormAttachment(btnClose, -6);
		fd_tblSentMail.right = new FormAttachment(tblNewMail, 0, SWT.RIGHT);
		fd_tblSentMail.top = new FormAttachment(lblSentMail, 6);
		fd_tblSentMail.left = new FormAttachment(tblNewMail, 0, SWT.LEFT);
		tblSentMail.setLayoutData(fd_tblSentMail);
		tblSentMail.setHeaderVisible(true);
		tblSentMail.setLinesVisible(true);
		
		TableColumn tblclmnTo = new TableColumn(tblSentMail, SWT.NONE);
		tblclmnTo.setWidth(160);
		tblclmnTo.setText("To");
		
		TableColumn tblclmnSubject_1 = new TableColumn(tblSentMail, SWT.NONE);
		tblclmnSubject_1.setWidth(260);
		tblclmnSubject_1.setText("Subject");
		
		TableColumn tblclmnDate_1 = new TableColumn(tblSentMail, SWT.NONE);
		tblclmnDate_1.setWidth(114);
		tblclmnDate_1.setText("Date");
		
		TableColumn tblclmnReceived = new TableColumn(tblSentMail, SWT.NONE);
		tblclmnReceived.setWidth(95);
		tblclmnReceived.setText("Received");
		createContents();
	}
	
	//Jacob Burkamper
	RecvMailWindow(Display display, DHEncryptionIO serverConnection)
	{
		this(display);
		this.io = serverConnection;
	}
	
	//Jacob Burkamper
	@Override
	public void open()
	{
		super.open();
	}

	/**
	 * Create contents of the shell.
	 */
	//Jacob Burkamper
	protected void createContents() {
		setText("SecMail");
		setSize(661, 496);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	//Evan Schirle
	private void getNewNotifications()
	{		
		PacketHeader getNotificationsHeader = new PacketHeader(Command.GET_NOTIFICATION);
		try {
			io.writeObject(getNotificationsHeader);
		} catch (IOException e1) {
			System.out.println("failed to send request");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try{
			PacketHeader notifPacket = (PacketHeader) io.readObject();
			System.out.println(notifPacket.getCommand());		//NO_NOTIFICATION
			//not sure what to do with the packet header... 
			
			LinkedList notifications = (LinkedList) io.readObject();
			while(!notifications.isEmpty()){
				Notification n = (Notification) notifications.pop();
				System.out.println(n.getType());	//NEW_EMAIL or EMAIL_RECEIVED
				//if type is EMAIL_RECEIVED the email is "no longer on the server"
				this.addNewTableItem(tblNewMail, n, false);
				//TODO distinguish between newMail and sentMail, put in corresponding table.
			}
		}catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Jacob Burkamper
	private void addNewTableItem(Table t, Notification n, boolean isOnDisk)
	{
		TableItem item = new TableItem(t, SWT.NONE);
		item.setData(n); // store the notification for future use
		item.setText(0, n.getFrom().compile()); // the from field
		item.setText(1, n.getSubject()); // the subject
		item.setText(2, DateFormat.getDateTimeInstance().format(n.getDate())); // the date, as a string
		
		//display if we have already gotten this field
		if (isOnDisk)
			item.setText(3, "Yes");
		else
			item.setText(3, "No");
	}
	
	//Opens the email associated with the notification n
	//will possibly fetch that email from the remote server if necessary
	
	//Yovana
	private void OpenOrFetchMail(TableItem item) throws ClassNotFoundException, IOException
	{
		Notification n = (Notification)item.getData();
		//--------------------------------------------------------------
		//get mail from local system
		
		File f = new File(MainWindow.getMailDir() + n.getID());
		if (f.exists()){
			System.out.println(f);
			//open the mail in the mail reader window here
			EmailStruct email = new EmailStruct(f);
				
			EmailReader reader = new EmailReader(Display.getCurrent(), email, n.getFrom(), n.getDate(), io);
			reader.open();
		}
		//--------------------------------------------
		//get mail from server
		else
		{
			//make packet header to send to server
			PacketHeader getEmailHeader = new PacketHeader(Command.RECEIVE_EMAIL);
			//send packet header to server 
			io.writeObject(getEmailHeader);
			//send ID to server 
			io.writeObject(n.getID());
			//send from user to server for email receipt
			io.writeObject(n.getFrom());
			
			PacketHeader responsePacket = (PacketHeader) io.readObject();
			if(responsePacket.getCommand() == Command.RECEIVE_EMAIL){
				//server sends back the email / packet header
				EmailStruct email = (EmailStruct)io.readObject();
				email.writeToFile(f);
				item.setText(3, "Yes"); // update the table item to show that the email has been downloaded
				
				//open in mail reader
				EmailReader reader = new EmailReader(Display.getCurrent(), email, n.getFrom(), n.getDate(), io);
				reader.open();			
			}
			else{
				noEmailOnServer();
			}
			}
		}
		
		//Josh Clark
		private void noNotificationsMessageBox()
		{
			Shell noNotifications = new Shell();
			MessageBox messageBox = new MessageBox(noNotifications, SWT.OK);
			messageBox.setText("No Notifications");
			messageBox.setMessage("You have no notifications!");		
			messageBox.open();
		}
		
		//Josh Clark
		private void noEmailOnServer()
		{
			Shell noEmail = new Shell();
			MessageBox messageBox = new MessageBox(noEmail, SWT.OK);
			messageBox.setMessage("Email is no longer on server. Sorry!");		
			messageBox.open();
		}
}

