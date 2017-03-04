package edu.depaul.secmail.content;

import edu.depaul.secmail.HttpSession;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class SignOut extends ResponseContent {
	public SignOut(MailServerConnection c) {
		super(false, null);
		String sessionID = c.getSessionID();
		c.close();
		HttpSession.remove(sessionID);
		setContent("<html><head>"
				+ "<meta http-equiv='Refresh' content='3;url=/'>"
				+ "</head>Logged out... Redirecting...</html>");
	}
}
