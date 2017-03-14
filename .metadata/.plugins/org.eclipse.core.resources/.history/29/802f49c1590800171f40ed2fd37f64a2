package edu.depaul.secmail.models;

import java.util.ArrayList;

public class Inbox {
	
	private ArrayList<Message> inbox = new ArrayList<Message>();
	
	public Inbox(){
		
	}
	
	public void addMessage(Message message){
		inbox.add(message);
	}
	
	public Message getMessage(int index){
		return inbox.get(index);
	}
	
	public boolean hasMessageWithID(int messageID){
		for (Message message : inbox){
			if (message.getID() == messageID){
				return true;
			}
		}
		return false;
	}
	
	public Message findMessageByID(int messageID){
		if (hasMessageWithID(messageID)){
			for (Message message : inbox){
				if (message.getID() == messageID){
					return message;
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
		return null;
	}
	
	public ArrayList<Message> getMessagesWithTag(Tag tag){
		ArrayList<Message> taggedMessages = new ArrayList<Message>();
		for (Message message: inbox){
			if (message.getTags().contains(tag)){
				taggedMessages.add(message);
			}
		}
		return taggedMessages;
	}

}
