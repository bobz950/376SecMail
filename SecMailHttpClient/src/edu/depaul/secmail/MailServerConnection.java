package edu.depaul.secmail;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MailServerConnection extends Thread {
	private Socket s;
	public DHEncryptionIO secIO;
	private String sessionID;
	private String user;
	private Map<String, EmailStruct> mailCache;
	
	public MailServerConnection(String session, String user, Socket s, DHEncryptionIO io) {
		this.user = user;
		this.sessionID = session;
		this.s = s;
		this.secIO = io;
		mailCache = new HashMap<String, EmailStruct>();
	}
	
	
	public void run() { 
		//use thread to ensure that the connection stayed open after logging in, then terminate thread
		try {
			secIO.writeObject(new PacketHeader(Command.CONNECT_TEST));
			PacketHeader packet = (PacketHeader)secIO.readObject();
			if (packet.getCommand() == Command.CONNECT_SUCCESS) System.out.println("Successfully connected client and started a new session");
			else {
				close();
				HttpSession.remove(this.sessionID);
			}
		}
		catch (IOException| ClassNotFoundException e) {
			close();
			HttpSession.remove(this.sessionID);
			System.out.println("Error maintaining connection to main server");
		}
	}
	
	public void close() {
		try {
			secIO.writeObject(new PacketHeader(Command.CLOSE));
			secIO.close();
			s.close();
		} 
		catch (IOException e) { return; }
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getSessionID() {
		return this.sessionID;
	}
	
	public void addToMailCache(String ID, EmailStruct e) {
		//only add to cache if the cache has 15 or less items, so memory usage isn't crazy insane
		if (mailCache.size() < 16) mailCache.put(ID, e);
	}
	
	public boolean isCached(String ID) {
		return mailCache.containsKey(ID);
	}
	
	public EmailStruct getFromCache(String ID) {
		return mailCache.get(ID);
	}
	
	//Evan Schirle
	public LinkedList<Notification> getNewNotifications()
	{		
		PacketHeader getNotificationsHeader = new PacketHeader(Command.GET_NOTIFICATION);
		try {
			secIO.writeObject(getNotificationsHeader);
		} 
		catch (IOException e1) {
			System.out.println("failed to send request");
		}
		
		try{
			PacketHeader notifPacket = (PacketHeader) secIO.readObject();
			
			if(!notifPacket.getCommand().equals(Command.NO_NOTIFICATIONS)){
				return null;
			}
			else {
				@SuppressWarnings("unchecked")
				LinkedList<Notification> notifications = (LinkedList<Notification>) secIO.readObject();
				return notifications;
			}

		}catch (ClassNotFoundException | IOException e) {
			System.out.println("error receiving notifications");
			return null;
		}
	}
	
	
	
	//This is where we add methods for making requests for data from the main server
}
