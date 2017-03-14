// Will Pankiewicz
package edu.depaul.secmail.models;

import java.util.ArrayList;

import edu.depaul.secmail.Notification;

public class Inbox {
	
	private ArrayList<DBNotification> inbox = new ArrayList<DBNotification>();
	private User user;
	
	public Inbox(){
		
	}
	
	
	// gets a message from a notification
	public void getMessageFromNotification(DBNotification notification){
		String sql = "SELECT * FROM notification WHERE message_id = "+ notification.getEmailID() + " AND user_id = "+ user.getID() + ";";
		inbox.add(DBNotification.getNotificationFromSQLStatement(sql));
	}
	
	// Returns a notification by its index
	public DBNotification getNotificationByIndex(int index){
		return inbox.get(index);
	}
	
	// Boolean that tests if an inbox has a message with an id
	public boolean hasMessageWithID(String messageID){
		for (DBNotification notification : inbox){
			if (notification.getEmailID() == messageID){
				return true;
			}
		}
		return false;
	}
	
	// returns a DBNotification in an inbox by its index in the arraylist
	public DBNotification findMessageByID(String messageID){
		if (hasMessageWithID(messageID)){
			for (DBNotification message : inbox){
				if (message.getEmailID() == messageID){
					return message;
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
		return null;
	}
	
	// Returns an arraylist of DBNotifications in an inbox with a given tag
	public ArrayList<DBNotification> getDBNotificationsWithTag(Tag tag){
		ArrayList<DBNotification> taggedMessages = new ArrayList<DBNotification>();
		for (DBNotification notification: inbox){
			if (notification.getTagsFromDBNotification().contains(tag)){
				taggedMessages.add(notification);
			}
		}
		return taggedMessages;
	}

	// Returns an arraylist of Messages in an inbox with a given tag
	public ArrayList<Message> getMessagessWithTag(Tag tag){
		ArrayList<Message> taggedMessages = new ArrayList<Message>();
		for (DBNotification notification: inbox){
			if (notification.getTagsFromDBNotification().contains(tag)){
				taggedMessages.add(notification.getMessageByDBNotification());
			}
		}
		return taggedMessages;
	}
}
