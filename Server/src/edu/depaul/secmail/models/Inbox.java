// Will Pankiewicz
package edu.depaul.secmail.models;

import java.util.ArrayList;

import edu.depaul.secmail.Notification;

public class Inbox {
	
	private ArrayList<Notification> inbox = new ArrayList<Notification>();
	private User user;
	
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
