package edu.depaul.secmail.content;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;

import edu.depaul.secmail.Command;
import edu.depaul.secmail.EmailStruct;
import edu.depaul.secmail.HttpHandler;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.Main;
import edu.depaul.secmail.PacketHeader;
import edu.depaul.secmail.ResponseContent;

public class ReadEmail extends ResponseContent {
		
	private String ID;
	private String sender;
	private String date;
	private String pass;
	public ReadEmail(MailServerConnection c, HttpHandler handler) {
		super(true, c);
		requestHeaders = handler.getRequestHeaders();
		ID = super.requestHeaders.get("notificationid");
		sender = super.requestHeaders.get("emailfrom");
		date = requestHeaders.get("emaildate");
		pass = requestHeaders.get("emailpass");
		try {
			sender = URLDecoder.decode(sender, "UTF-8");
			date = URLDecoder.decode(date, "UTF-8");
		} catch (UnsupportedEncodingException e1) {}
		PacketHeader getEmailHeader = Main.makePH(Command.RECEIVE_EMAIL);
		try {
			mainConnection.secIO.writeObject(getEmailHeader);
			mainConnection.secIO.writeObject(ID);
			mainConnection.secIO.writeObject(Main.makeUser(sender));
			PacketHeader response = (PacketHeader) mainConnection.secIO.readObject();
			if (response.getCommand() == Command.RECEIVE_EMAIL) {
				EmailStruct e = (EmailStruct)mainConnection.secIO.readObject();
				showEmail(e);
			}
			else setContent("<b>Could not open email</b>");
		} catch (IOException | ClassNotFoundException e) {}
	}
	
	private void showEmail(EmailStruct e) {
		if (!e.isEncrypted()) show(e);
		else {
			//prompt for password
			if (pass == null) {
				this.mainConnection.addToMailCache(ID, e); //add to cache so we don't have to go back to server for decrypt attempt
				setContent("something to prompt for a password here...");
			}
			else if (attemptDecrypt(e)) show(e);
			else setContent("Email password was incorrect");
		}
		
	}
	
	private boolean attemptDecrypt(EmailStruct e) {
		return e.decrypt(pass);
	}
	
	private void show(EmailStruct e) {
		setContent("<b>Message sent by " + sender +" on " + date);
		addContent("<br>Subject: " + e.getSubject() + "<br>");
		addContent("Message: </b><br>" + e.getBody());
	}
	
}
