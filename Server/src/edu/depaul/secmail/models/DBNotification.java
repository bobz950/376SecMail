// WILL PANKIEWICZ

package edu.depaul.secmail.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.Notification;
import edu.depaul.secmail.NotificationType;
import edu.depaul.secmail.SecMailServer;

public class DBNotification {
	
	private String notificationID;
	private User sender;
	private User recipient;
	private String subject;
	private String emailID;
	private Date sendDate;

	// Constructor to match the db
	public DBNotification(String notificationID,User sender, User recipient, String subject, String emailID, Date sendDate) {
		this.notificationID = notificationID;
		this.sender = sender;
		this.recipient = recipient;
		this.subject = subject;
		this.emailID = emailID;
		this.sendDate = sendDate;
	}
	
	public DBNotification(User sender, User recipient, String subject, String emailID, Date sendDate) {
		this.sender = sender;
		this.recipient = recipient;
		this.subject = subject;
		this.emailID = emailID;
		this.sendDate = sendDate;
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

	public String getEmailID() {
		return emailID;
	}

	public Date getSendDate() {
		return sendDate;
	}

	@Override
	public String toString() {
		return "Notification [sender=" + sender + ", recipient=" + recipient + ", subject=" + subject + ", emailID="
				+ emailID + ", sendDate=" + sendDate + "]";
	}
	
	public Notification toNotificatonStruct(){
		return SecMailServer.makeNotification(recipient.toUserStruct(), sender.toUserStruct(), NotificationType.NEW_EMAIL, emailID, subject, sendDate);
	}
	
	public void dbWrite() {

		String sql = "INSERT INTO notification VALUES (0,  \"" + sender.getID() + "\", \"" + recipient.getID() + "\", \"" + emailID + "\" , \"" + new java.sql.Date(sendDate.getTime()) + "\")";
		System.out.println(sql);
		java.sql.Connection conn = null;
		PreparedStatement stmt = null;
		
		try{
			// Open a Connection
			System.out.println("Connecting to database...");
			conn = DBCon.getRemoteConnection();
			
			// Execute query 
			System.out.println("Creating statement...");
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			
			// set tagID from newly inserted row
			if (rs.next()){
				System.out.println("Insert Notification Success");
				notificationID = rs.getString(1);
			}
			
			// Clean up connection
			stmt.close();
			conn.close();
		} catch (SQLException se){
			// handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e){
			// handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// close resources
			try{
				if (stmt != null) stmt.close();
			} catch (SQLException se2){}
			try {
				if (conn != null) conn.close();
			} catch (SQLException se){
				se.printStackTrace();
			}
		}
	
	}
	
	// Returns the tags of a current message from a DBNotification
	public ArrayList<Tag> getTagsFromDBNotification(){
		Message message = Message.getMessageByNotification(this);
		return message.getTags();
	}
	
	// Static method of returning tags from Notification
	public static ArrayList<Tag> getTagsFromDBNotification(DBNotification notification){
		return notification.getTagsFromDBNotification();
	}
	
	// returns a message based on the current notification object
	public Message getMessageByDBNotification(){
		return Message.getMessageByID(emailID);
	}
	
	// Static method to return a message object from a dbnotification object 
	public static Message getMessageByDBNotification(DBNotification notification){
		return Message.getMessageByID(notification.getEmailID());
	}
	
	//Returns a DBNotification object based on its id in the database
	public static DBNotification getDBNotificationByID(String dbNotificationID){
		String sql = "SELECT * FROM notification where notification_id = \"" + dbNotificationID + "\"";
		return getNotificationFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a recipient User object
	public static ArrayList<DBNotification> getDBNotificationsByRecipient(User recipient){
		String sql = "SELECT * FROM notification where recipient_id = \"" + recipient.getID() + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a recipient id
	public static ArrayList<DBNotification> getDBNotificationsByRecipientID(String recipientID){
		String sql = "SELECT * FROM notification where recipient_id = \"" + recipientID + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a recipient address
	public static ArrayList<DBNotification> getDBNotificationsByRecipientAddress(String recipientAddress){
		String sql = "SELECT * FROM notification where recipient_id = \"" + User.getUserFromAddress(recipientAddress).getID() + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a sender User object
	public static ArrayList<DBNotification> getDBNotificationsBySender(User sender){
		String sql = "SELECT * FROM notification where sender_id = \"" + sender.getID() + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a sender id
	public static ArrayList<DBNotification> getDBNotificationsBySenderID(String senderID){
		String sql = "SELECT * FROM notification where sender_id = \"" + senderID + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	//Returns an array of DBNotification objects based on a sender address
	public static ArrayList<DBNotification> getDBNotificationsBySenderAddress(String senderAddress){
		String sql = "SELECT * FROM notification where sender_id = \"" + User.getUserFromAddress(senderAddress).getID() + "\"";
		return getNotificationArrayListFromSQLStatement(sql);
	}
	
	// Returns a DBNotification object based on a valid sql query 
	public static DBNotification getNotificationFromSQLStatement(String sql){
		Connection conn = null;
		Statement stmt = null;
		DBNotification notification = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = DBCon.getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.createStatement();
	
			ResultSet rs = stmt.executeQuery(sql);
			
			// Extract data from result set
			while (rs.next()){
				String id = rs.getString("notification_id");
				User sender= User.getUserFromID(rs.getInt("sender_id"));
				User recipient= User.getUserFromID(rs.getInt("recipient_id"));
				Message message = Message.getMessageByID(rs.getString("message_id"));
				Date messageDate = rs.getDate("message_date");
		
				notification = new DBNotification(id, sender, recipient, message.getSubject(), new Integer(message.getID()).toString(), messageDate);
			}
			
			// clean up connection
			rs.close();
			stmt.close();
			conn.close();
		} catch(SQLException se){
			// handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e){
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try{
				if (stmt != null)
					stmt.close();
				} catch(SQLException se2){
				}
				try{
					if (conn != null) conn.close();
				} catch (SQLException se){
					se.printStackTrace();
				}
		}
		return notification;
	}
	
	// Returns a DBNotification arraylist based on a valid sql query 
	public static ArrayList<DBNotification> getNotificationArrayListFromSQLStatement(String sql){
		Connection conn = null;
		Statement stmt = null;
		DBNotification notification = null;
		ArrayList<DBNotification> dbNotificationArrayList = new ArrayList<DBNotification>();
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = DBCon.getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.createStatement();
	
			ResultSet rs = stmt.executeQuery(sql);
			
			// Extract data from result set
			while (rs.next()){
				String id = rs.getString("notification_id");
				User sender= User.getUserFromID(rs.getInt("sender_id"));
				User recipient= User.getUserFromID(rs.getInt("recipient_id"));
				Message message = Message.getMessageByID(rs.getString("message_id"));
				Date messageDate = rs.getDate("message_date");
		
				notification = new DBNotification(id, recipient, recipient, message.getSubject(), message.getMessageID(), messageDate);
				dbNotificationArrayList.add(notification);
			}
			
			// clean up connection
			rs.close();
			stmt.close();
			conn.close();
		} catch(SQLException se){
			// handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e){
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try{
				if (stmt != null)
					stmt.close();
				} catch(SQLException se2){
				}
				try{
					if (conn != null) conn.close();
				} catch (SQLException se){
					se.printStackTrace();
				}
		}
		return dbNotificationArrayList;
	}
	
	

}
