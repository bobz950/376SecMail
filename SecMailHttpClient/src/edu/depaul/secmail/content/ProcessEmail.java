package edu.depaul.secmail.content;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import edu.depaul.secmail.Command;
import edu.depaul.secmail.EmailStruct;
import edu.depaul.secmail.HttpHandler;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.Main;
import edu.depaul.secmail.PacketHeader;
import edu.depaul.secmail.ResponseContent;

public class ProcessEmail extends ResponseContent {
	private String to;
	private String subject;
	private String message;
	
	public ProcessEmail(MailServerConnection c, HttpHandler h) {
		super(true, c);
		this.requestHeaders = h.getRequestHeaders();
		to = requestHeaders.get("emailrecipient");
		subject = requestHeaders.get("emailsubject");
		message = requestHeaders.get("emailmessage");
		if (send()) {
			setContent("<b>Email sent successfully!</b>");
			addContent("<br><a href='/'>Return to your inbox</a>");
		}
		else setContent("There was en error sending your email");
	}
	
	private boolean send() {
		try {
			to = URLDecoder.decode(to, "UTF-8");
			subject = URLDecoder.decode(subject, "UTF-8");
			message = URLDecoder.decode(message, "UTF-8");
		} catch (UnsupportedEncodingException e2) {}
		String[] recps = {};
		boolean mult = false;
		if (to.indexOf(",") > 0) {
			to = to.replaceAll("\\s+", "");
			recps = to.split(",");
			if (recps.length > 1) mult = true;
		}
		EmailStruct e = Main.makeEmail();
		if (mult) {
			for (String s : recps) e.addRecipient(s);
		}
		else e.addRecipient(to);
		e.setSubject(subject);
		e.setBody(message);
		
		String enc;
		String encPass;
		if ((enc = requestHeaders.get("emailencrypted")) != null) {
			encPass = requestHeaders.get("emailpassword");
			try {
				encPass = URLDecoder.decode(encPass, "UTF-8");
			} 
			catch (UnsupportedEncodingException e1) {}
			e.encrypt(encPass);
		}
		
		PacketHeader emailPacket = Main.makePH(Command.SEND_EMAIL);
		try {
			mainConnection.secIO.writeObject(emailPacket);
			mainConnection.secIO.writeObject(e);
			mainConnection.secIO.writeObject(Main.makePH(Command.END_EMAIL));
			PacketHeader response = (PacketHeader)mainConnection.secIO.readObject();
			if (response.getCommand() == Command.CONNECT_SUCCESS) return true;
			else return false;
		} catch (IOException | ClassNotFoundException e1) {return false;}
	}

}
