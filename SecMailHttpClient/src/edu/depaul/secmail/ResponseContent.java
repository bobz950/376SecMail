
//This abstract class is meant to be extended by classes that produce the content to be output to the browser
package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ResponseContent implements Content {
	
	private String htmlHeader = "";
	private String htmlFooter = "";
	private boolean useHtmlWrapper; //set to true if content should be wrapper in predefined html header and footer
	protected MailServerConnection mainConnection; //main connection to the main server
	private String content = "";
	protected HashMap<String, String> requestHeaders;//headers and parameters supplied by this request
	private boolean responseAdded = false;//set to true if headers should be added to the response
	private ArrayList<String> addedResponseHeaders;//any added response headers
	
	public ResponseContent(boolean useWrapper, MailServerConnection c) {
		this.useHtmlWrapper = useWrapper;
		this.mainConnection = c;
		try {
			BufferedReader r = new BufferedReader(new FileReader("layout/templateHeader.html"));
			String l;
			while((l = r.readLine()) != null) htmlHeader += l;
			r.close();
			r = new BufferedReader(new FileReader("layout/templateFooter.html"));
			while((l = r.readLine()) != null) htmlFooter += l;
			r.close();
		} 
		catch (IOException e) {}
		
	}

	public void setContent(String s) {
		this.content = s;
	}
	
	public void addContent(String s) {
		this.content = this.content.concat(s);
	}
	
	public String display() {
		if (this.useHtmlWrapper) {
			StringBuilder content = new StringBuilder();
			content.append(this.htmlHeader);
			content.append(this.content);
			content.append(this.htmlFooter);
			return content.toString();
		}
		else return this.content;
	}
	
	public void addToResponseHeader(String s) {
		//add a single header to the response
		if (!responseAdded) {
			this.addedResponseHeaders = new ArrayList<String>();
			this.responseAdded = true;
		}
		this.addedResponseHeaders.add(s);
	}
	
	public ArrayList<String> getAddedReponseHeaders() {
		if (responseAdded) return this.addedResponseHeaders;
		else return null;
	}

}
