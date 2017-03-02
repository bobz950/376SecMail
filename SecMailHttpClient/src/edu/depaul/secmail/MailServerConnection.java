package edu.depaul.secmail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MailServerConnection extends Thread {
	private Socket s;
	private DHEncryptionIO secIO;
	private String sessionID;
	private String user;
	
	public MailServerConnection(String session, String user) {
		this.user = user;
		this.sessionID = session;
	}
	
	
	public void run() {
		try {
			this.s = new Socket(Main.host, Main.port);
			this.secIO = new DHEncryptionIO(s, false);
		} 
		catch (UnknownHostException e) { System.out.println("Could not connect to host"); } 
		catch (IOException e) { System.out.println("IOException while creating input/output stream"); }
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
