package edu.depaul.secmail.content;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class Home extends ResponseContent {

	public Home(MailServerConnection c) {
		super(true, c);
		setContent("<p><b>Welcome to the home page</b></p>");
		addContent("Bye bye");
	}
}
