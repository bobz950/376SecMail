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
		// Melvin Kurian
		String homePageHtml;
		FileReader fr = null;
		try {
			fr = new FileReader("layout/homePage.html");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		StringBuilder content = new StringBuilder(1024);
		try {
			while ((homePageHtml = br.readLine()) != null) {
				content.append(homePageHtml);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		homePageHtml = content.toString();

		setContent(homePageHtml);
	}
}
