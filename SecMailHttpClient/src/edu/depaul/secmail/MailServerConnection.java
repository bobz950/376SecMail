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
