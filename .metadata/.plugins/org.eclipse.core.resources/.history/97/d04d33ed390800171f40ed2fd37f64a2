package edu.depaul.secmail.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import edu.depaul.secmail.EmailStruct;

public class Message implements DBModel {

	private final int messageID;
	private final User sender;
	private final User recipient;
	private final String subject;
	private final String content;
	// Message Attachment
	private final Date messageDate;
	private final ArrayList<Tag> tags;
	private boolean hasRead;
	
	//public Message(){}
	
	public Message(int messageID, User sender, User recipient, String subject, String content, Date messageDate){
		this.messageID = messageID;
		this.sender = sender;
		this.recipient = recipient;
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
		tags = new ArrayList<Tag>();
		hasRead = false;
	}

	public User getSender() {
		return sender;
	}

	public User getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public ArrayList<Tag> getTags() {
		return tags;
	}
	
	public void addTag(Tag tag){
		tags.add(tag);
	}

	@Override
	public int getID() {
		return messageID;
	}

	@Override
	public void encrypt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void decrypt() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dbWrite() {
		// TODO Auto-generated method stub
		
	}

}
