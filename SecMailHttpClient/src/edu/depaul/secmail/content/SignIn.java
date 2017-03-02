package edu.depaul.secmail.content;

import java.util.HashMap;

import edu.depaul.secmail.HttpHandler;
import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class SignIn extends ResponseContent {
	public SignIn(MailServerConnection c, HttpHandler handler) {
		super(false, c);
		this.requestHeaders = handler.getRequestHeaders();
		
		String name = this.requestHeaders.get("name");
		String pass = this.requestHeaders.get("pass");
		//msg = "Name: " + name + " Password: " + pass;
		if (handler.handleLogin(name, pass)) setContent("<html><head>"
				+ "<meta http-equiv='Refresh' content='3;url=/'>"
				+ "</head>Login Success! Redirecting...</html>");
		else setContent("Invalid Username / Password");
	}
}
