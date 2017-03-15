/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import edu.depaul.secmail.models.DBNotification;
import edu.depaul.secmail.models.Message;
import edu.depaul.secmail.models.User;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SecMailServer {
	
	
	private static Config serverConfig;
	public static LinkedList<Notification> notifications;
	private static ObjectOutputStream notificationWriter = null;
	
	//Jacob Burkamper
	public static void main(String[] args) {
		
		// read the command line arguments and set up the configuration
		System.out.println("SecMaild Starting..."); // just output a message so something is on the console.
		serverConfig = new Config(args);
		Log.Init(serverConfig.getLogFile());
		
		Log.Out("Set up Server config and log file.");
		
		Log.Out("Reading notification list from file system");
		loadNotifications();
		
		Log.Out("Using Mail Directory: "+serverConfig.getMailRoot());
		checkAndCreateMailDir();
		
		Log.Out("Binding to port " + serverConfig.getPort() +" backlog " + serverConfig.getBacklog());
		try (
			ServerSocket serverSocket = new ServerSocket(serverConfig.getPort(), serverConfig.getBacklog());
		){
			while (true)
			{
				Socket clientSocket = serverSocket.accept();
				Log.Debug("Connected to client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
				
				//process the client
				(new Thread(new ClientHandler(clientSocket))).start();
			}
		}catch (IOException e) {
			//This will need to be handled better later. For now, just dump the error and crash.
			System.err.println(e);
			System.exit(1); 
		}

	}
	
	
	//Robert Alianello
	private static void loadNotifications()
	{
		notifications = new LinkedList<Notification>(); // instantiate empty list
		
		ArrayList<DBNotification> dbNots = DBNotification.getNotificationArrayListFromSQLStatement("SELECT * FROM notification;");
		
		for (DBNotification n : dbNots) {
			Notification notification = n.toNotificatonStruct();
			notifications.add(notification);
		}
	
	}
	
	//save the notifications list to db
	//Robert Alianello
	private static void saveNotification(Notification n) 
	{
		DBNotification notification = new DBNotification(new User(n.getFrom().getUser()), new User(n.getTo().getUser()), n.getSubject(), n.getID(), new java.sql.Date(n.getDate().getTime()));
		if (n.getType() == NotificationType.EMAIL_RECEIVED) notification.setTypeReceived();
		notification.dbWrite();
	}
	
	//Jacob Burkamper
	public static synchronized LinkedList<Notification> getNotificationList(UserStruct username)
	{
		//search the notification list and return a new linked list containing notifications for the user only
		LinkedList<Notification> ret = new LinkedList<Notification>();
//		System.out.println("Searching for notifications... for: "+username.compile());
		for (Notification n : notifications){
//			System.out.println(n.toString());
			if (n.getTo().compile().trim().equals(username.compile().trim())){
				ret.add(n);
			}
		}
		
		return ret;
	}
	
	//Robert Alianello
	public static synchronized void addNotificationToList(Notification n)
	{		
		//check if there is a duplicate notification
		if (notificationContains(n)) return;
		//if not, add the notification to the list.	
		notifications.add(n);
		
		saveNotification(n); // save the notification to disk
		return;
	}
	//Robert Alianello
	public static boolean notificationContains(Notification n) {
		Optional<Notification> f = new ArrayList<Notification>(notifications).stream().filter(No -> (No.getTo().compile().trim().equals(n.getTo().compile().trim())) 
				&& (No.getFrom().compile().trim().equals(n.getFrom().compile().trim())) && 
				(No.getID().equals(n.getID())) && (No.getType() == n.getType()))
				.findFirst();
		if (f.isPresent()) return true;
		else return false;
	}
	
	//returns the Config object being used by the server
	//Jacob Burkamper
	public static Config getGlobalConfig()
	{
		return serverConfig;
	}
	
	private static void checkAndCreateMailDir()
	{
		File mailDir = new File(serverConfig.getMailRoot());
		if (!mailDir.exists())
		{
			Log.Out("Creating Mail directory" + mailDir.getAbsolutePath());
			if (!mailDir.mkdir()) // if the dir doesn't get made
			{
				Log.Error("Couldn't create mail directory. Unable to continue");
				System.exit(10);
			}
		}
	}	
	
	//WP
	public static UserStruct makeUser(String s){
		return new UserStruct(s);
	}
	
	//WP
	public static Notification makeNotification(UserStruct toUser, UserStruct fromUser, NotificationType type, String id, String subject, Date date)
	{
		return new Notification(toUser, fromUser, type, id, subject, date);
	}
	
	//WP
	public static EmailStruct makeEmail(){
		return new EmailStruct();
	}

}
