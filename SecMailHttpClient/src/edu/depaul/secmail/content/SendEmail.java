package edu.depaul.secmail.content;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.depaul.secmail.MailServerConnection;
import edu.depaul.secmail.ResponseContent;

public class SendEmail extends ResponseContent {
	public SendEmail(MailServerConnection c) {
		super(true, c);
		setContent(loadForm());
	}
	
	private String loadForm() {
		StringBuilder res = new StringBuilder();
		String s;
		try {
			BufferedReader r = new BufferedReader(new FileReader("layout/sendEmailForm.html"));
			while ((s = r.readLine()) != null) res.append(s);
			return res.toString();
		} 
		catch (IOException e) {return "Error";}
	}
}
