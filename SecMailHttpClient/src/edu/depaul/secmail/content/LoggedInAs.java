package edu.depaul.secmail.content;

import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class LoggedInAs extends ResponseContent {
	public LoggedInAs(MailServerConnection c) {
		super(true, c);
		
		setContent("<b>You are logged in as: " + this.mainConnection.getUser() + "<br>");
		addContent("Your email address is: " + this.mainConnection.getUser() + "@" + edu.depaul.secmail.Main.host + "</b>");
	}
}
