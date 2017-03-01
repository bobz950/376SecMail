package edu.depaul.secmail.content;

import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class LoggedInAs extends ResponseContent {
	public LoggedInAs(MailServerConnection c) {
		super(true, c);
		
		setContent("You are logged in as: " + this.mainConnection.getUser());
	}
}
