package edu.depaul.secmail.content;

import edu.depaul.secmail.HttpSession;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class SignOut extends ResponseContent {
	public SignOut(MailServerConnection c) {
		super(false, c);
		String sessionID = c.getSessionID();
		HttpSession.remove(sessionID);
		setContent("<html><head>"
				+ "<meta http-equiv='Refresh' content='3;url=/'>"
				+ "</head>Logged out... Redirecting...</html>");
	}
}
