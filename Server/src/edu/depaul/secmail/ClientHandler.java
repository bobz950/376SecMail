/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.util.LinkedList;
import java.util.concurrent.*;

import edu.depaul.secmail.models.Message;
import edu.depaul.secmail.models.User;

import java.net.Socket;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class ClientHandler implements Runnable{
	private Socket clientSocket = null;
	private DHEncryptionIO io = null;
	UserStruct user = null;
	Config config = null;
	
	//Jacob Burkamper
	ClientHandler(Socket s)
	{
		this.clientSocket = s;
		try {
			io = new DHEncryptionIO(clientSocket, true);
		} catch (IOException e) {
			//TODO: handle this. For now, just output error and abort
			System.err.println(e);
			Log.Error("Exception creating ClientHandler: ");
			Log.Error(e.toString());
			System.exit(10);
		}
		
		
	}
	
	//Jacob Burkamper
	public void run()
	{
		Log.Debug("Starting ClientHandler");
	
		try {
			PacketHeader nextPacket = null;
			while ((nextPacket = (PacketHeader)io.readObject()) != null) {		
		        if (nextPacket.getCommand() == Command.CLOSE){  
		        	break; // leave the loop
		        }  
		        else{
		        	processPacket(nextPacket);
		        }
		        	
		    }
		} catch (IOException e) {
			Log.Error("Error while trying to read or write to socket");
			Log.Error(e.toString());
		} catch (ClassNotFoundException e)
		{
			Log.Error("Error while trying to get object from network. Class not found");
			Log.Error(e.toString());
		}
		
		//we're done. close the stuff
		try {
			Log.Debug("Closing connection to client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			io.close();
			clientSocket.close();
		} catch (IOException e) {
			System.err.println(e);
			Log.Error(e.toString());
		}
	}
	
	//Collective effort from all group memebers
	private void processPacket(PacketHeader ph)
	{
		Log.Debug("Processing packet for command " + ph.getCommand());
	
		switch(ph.getCommand()){
			case CONNECT_TEST:
				handleTestConnection();
				break;
			case LOGIN:
				handleLogin();
				break;
			case SEND_EMAIL:
				Log.Debug("Start Email Handler");
				handleEmail();
				break;
			case GET_NOTIFICATION:
				handleGetNotification();
				break;
			case SEND_NOTIFICATION:
				handleNewNotificationSent();
				break;
			case RECEIVE_EMAIL:
				retrieveEmail(getNotificationID(), getFromUser());
		default:
			break;
		}
	
	}
	
	//Jacob Burkamper
	private void handleTestConnection()
	{
		//create successful connection packet
		PacketHeader successfulTestPacket = new PacketHeader();
		successfulTestPacket.setCommand(Command.CONNECT_SUCCESS);
		
		try {
			io.writeObject(successfulTestPacket);
		} catch (Exception e)
		{
			Log.Error("Exception thrown." + e);
		}
	}
	
	//Jacob Burkamper
	private void handleLogin(){
		try {
			String username = (String)io.readObject();
			String password = (String)io.readObject();
			
                       	
			//authenticate
			if (User.authenticate(username,password)) 
			{
				io.writeObject(new PacketHeader(Command.LOGIN_SUCCESS));
				user = new UserStruct(username, SecMailServer.getGlobalConfig().getDomain(), SecMailServer.getGlobalConfig().getPort());
			}
			else
			{
				// Let the client know that the login was unauthenticated
				// Remain open and wait for next login packet
				io.writeObject(new PacketHeader(Command.LOGIN_FAIL));
			}
		} catch (IOException e)
		{
			Log.Error("IO Exception while trying to handle login");
			Log.Error(e.toString());
			
		} catch (ClassNotFoundException e)
		{
			Log.Error("Error while trying to get object from network. Class not found");
			Log.Error(e.toString());
		}
	}
	
	// Josh Clark
	private void handleEmail(){
		//die early if the user hasn't authenticated.
		if (user == null)
			return;
		
		//Read Email from input stream
		try {
			EmailStruct newEmail = (EmailStruct)io.readObject();
			
			LinkedList<Notification> newNotificationList = newEmail.getNotificationList(user);
			
			//spawn a new thread to handle sending out the notifications here
			(new Thread(new NotificationSender(newNotificationList))).start();
			
			receiveAttachments(newEmail);		
			storeEmail(newEmail);			
			
			PacketHeader successfulEmailPacket = new PacketHeader();
			successfulEmailPacket.setCommand(Command.CONNECT_SUCCESS);
			io.writeObject(successfulEmailPacket);
		
		} catch (ClassNotFoundException | IOException e) {
			Log.Error("Error thrown while trying to read User from client");
			e.printStackTrace();
		}
	}
	
	//Jacob Burkamper
	//receives the attachments associated with email from the client
	private void receiveAttachments(EmailStruct email)
	{
		try {
			PacketHeader hasAttachmentHeader = (PacketHeader)io.readObject();
			if (hasAttachmentHeader.getCommand() == Command.END_EMAIL)
				return; // no attachments, nothing to do.
			
			//handle the attachments
			for (
					PacketHeader header = (PacketHeader)io.readObject();
					header.getCommand() != Command.END_ATTACHMENTS;
					header = (PacketHeader)io.readObject()
					)
			{
				if (header.getCommand() != Command.SEND_ATTACHMENT)
					Log.Error("Protocol Error! Next header was not attachment send");
				
				//create a temporary file
				String tmpPath = SecMailServer.getGlobalConfig().getMailRoot() + user.getUser() + "/"
						+email.getID() + ".tmp-attach";
				File tmp = new File(tmpPath);
				FileOutputStream fos = new FileOutputStream(tmp);
				
				//for every array we are expecting
				for (int i = 0; i < header.getLength(); i++)
				{
					byte[] b = (byte[])io.readObject(); //read the array from the network
					fos.write(b); // write the array to the file
				}
				fos.close();
				//move the now-complete file from temp to its final location
				Path finalPath = Paths.get(SecMailServer.getGlobalConfig().getUserDirectory(user.getUser()) + email.getID() + "." + header.getString());
				Files.move(tmp.toPath(), finalPath, StandardCopyOption.ATOMIC_MOVE);
				
				//add the retrieved file to the email
				File finalFile = finalPath.toFile();
				email.addAttachment(finalFile);
			}
		} catch (ClassNotFoundException e)
		{
			Log.Error("ClassNotFound while receiving attachments");
			Log.Error(e.toString());
		} catch (IOException e) {
			Log.Error("IO Error while trying to read attachments");
			Log.Error(e.toString());
		}
	}
	
	//Clayton Newmiller
	private void handleGetNotification()
	{
		LinkedList<Notification> notifications = SecMailServer.getNotificationList(this.user);
	
		
		try {

			if(notifications.isEmpty()){
				PacketHeader noNotifications = new PacketHeader(Command.END_NOTIFICATION);
				this.io.writeObject(noNotifications);
			}
			else{
				this.io.writeObject(new PacketHeader(Command.NO_NOTIFICATIONS));
				this.io.writeObject(notifications);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//send all the notifications in the above linked list to the client.
	}
	
	//Jacob Burkamper
	private String getIdentifier()
	{
		return clientSocket.getInetAddress() + ":" + clientSocket.getPort();
	}
	
	//Robert Alianello
	private void storeEmail(EmailStruct email) throws IOException
	{
		Message dbEmail;
		if (!email.isEncrypted()) dbEmail = new Message(email.getID(), email.getSubject(), email.getBody());
		else {
			byte[] encryptedBody = email.getEncryptedBytes();
			dbEmail = new Message(email.getID(), email.getSubject(), encryptedBody);
		}
		LinkedList<UserStruct> recipients = email.getToList();
		//add all recipients
		for (UserStruct usr : recipients) dbEmail.addRecipient(new User(usr.getUser()));
		dbEmail.dbWrite();

		return;
	}
	
	//Jacob Burkamper
	private void handleNewNotificationSent() {
		try {
			Notification recievedNotification = (Notification)io.readObject();
			SecMailServer.addNotificationToList(recievedNotification);
		} catch (ClassNotFoundException e) {
			Log.Error("ClassNotFoundException thrown while trying to read a notification from another server");
			Log.Error(e.toString());
		} catch (IOException e) {
			Log.Error("IOException thrown while reading notification from remote server");
			Log.Error(e.toString());
		}
	}
	 
	// Robert Alianello
	private void retrieveEmail(String id, UserStruct fromUser)
	{
		if(user == null) return;
		else{
			
			//get the message from db
			Message msg = Message.getMessageByID(id);
			try {
				if (msg != null) {
					PacketHeader sendEmail = new PacketHeader(Command.RECEIVE_EMAIL);
					EmailStruct email = msg.toEmailStruct();
					Notification confirmation = new Notification(fromUser, user, NotificationType.EMAIL_RECEIVED, email);
					SecMailServer.addNotificationToList(confirmation);
					io.writeObject(sendEmail);
					io.flush();
					io.writeObject(email);
				}
				else {
					PacketHeader noEmail = new PacketHeader(Command.NO_EMAIL);
					io.writeObject(noEmail);
				}
			}
			catch (IOException e) {}			
		}
	}
	
	// Josh Clark
	private String getNotificationID(){
		
		// read in the id sent over from client
		try {
			String id = (String)io.readObject();
			return id;
		} catch (ClassNotFoundException e) {
			Log.Error("ClassNotFoundException thrown while trying to read Email ID from client");
			Log.Error(e.toString());
		} catch (IOException e) {
			Log.Error("IOException thrown while trying to read Email ID from client");
			e.printStackTrace();
		}
		return null;	
	}
	
	// Josh Clark
	private UserStruct getFromUser(){
		
		// read in the id sent over from client
		try {
			UserStruct fromUser = (UserStruct)io.readObject();
			return fromUser;
		} catch (ClassNotFoundException e) {
			Log.Error("ClassNotFoundException thrown while trying to read User from client");
			Log.Error(e.toString());
		} catch (IOException e) {
			Log.Error("IOException thrown while trying to read User from client");
			e.printStackTrace();
		}
		return null;	
	}
	
}
