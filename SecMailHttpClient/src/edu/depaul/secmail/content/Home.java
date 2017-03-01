package edu.depaul.secmail.content;

import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class Home extends ResponseContent {
	
	public Home(MailServerConnection c) {
		super(true, c);
		setContent("<h1>This is our homepage!..</h1>");
		addContent("It sucks right now...");
		addContent("<br><a href='/signout'>Sign Out</a>");
	}
}
