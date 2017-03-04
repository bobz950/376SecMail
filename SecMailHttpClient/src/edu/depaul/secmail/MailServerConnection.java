package edu.depaul.secmail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MailServerConnection extends Thread {
	private Socket s;
	private DHEncryptionIO secIO;
	private String sessionID;
	private String user;
	
	public MailServerConnection(String session, String user, Socket s, DHEncryptionIO io) {
		this.user = user;
		this.sessionID = session;
		this.s = s;
		this.secIO = io;
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
	
	//This is where we add methods for making requests for data from the main server
}
