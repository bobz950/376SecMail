package edu.depaul.secmail.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.EmailStruct;
import edu.depaul.secmail.SecMailServer;

public class Message implements DBModel {

	private String messageID;
	private User sender;
	private ArrayList<User> recipients = new ArrayList<User>();
	private String subject;
	private String content;
	private byte[] encryptedBytes =  null;
	private boolean encrypted = false;
	// Message Attachment
	private Date messageDate;
	private ArrayList<Tag> tags;
	private boolean hasRead;
	
	
	public Message(String messageID, User sender, User recipient, String subject, String content, Date messageDate){
		this.messageID = messageID;
		this.sender = sender;
		recipients.add(recipient);
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
		tags = new ArrayList<Tag>();
		hasRead = false;
	}
	
	public Message(User sender, User recipient, String subject, String content, Date messageDate){
		this.sender = sender;
		recipients.add(recipient);
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
	}
	
	// Constructor that matches DB, minus the attachment
	public Message(String messageID, String message_subject, String message_content){
		this.messageID = messageID;
		this.subject = message_subject;
		this.content = message_content;
	}

	public User getSender() {
		return sender;
	}

	public ArrayList<User> getRecipient() {
		return recipients;
	}
	
	public void addRecipient(User recipient){
		recipients.add(recipient);
	}

	@Override
	public String toString() {
		return "Message [messageID=" + messageID + ", sender=" + sender + ", recipient=" + recipients + ", subject="
				+ subject + ", content=" + content + ", messageDate=" + messageDate + ", tags=" + tags + ", hasRead="
				+ hasRead + "]";
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
		return Integer.parseInt(messageID);
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
		// the last parameter is an attachment BLOB field in the database
		//String messageSqlQuery = "INSERT INTO message VALUES (0, \"" + subject + "\", + \""+content + "\", null)";
		String messageSqlQuery = "INSERT INTO message (message_id, message_subject, message_content) VALUES (?, ?, ?)";
		//System.out.println(messageSqlQuery);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = DBCon.getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.prepareStatement(messageSqlQuery, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, messageID);
			stmt.setString(2, subject);
			stmt.setString(3, content);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			
			// Extract data from result set
			if (rs.next()){
				System.out.println("Insert message success");
				
				messageID = rs.getString(1);
				
				// Write into notifications table for each message recipient
				for (User recipient : recipients){
					DBNotification n = new DBNotification(sender, recipient, subject, rs.getString(1), messageDate);
					n.dbWrite();
				}
				
			} else {
				System.out.println("Insert message fail");
			}
			
			// clean up connection
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
	}
	
	// FOR TESTING PURPOSES ONLY!!!!!!!!!!! Or not..... ;)
	public static void destroyAllMessages(){
			// the last parameter is an attachment BLOB field in the database
			String messageSqlQuery = "TRUNCATE TABLE message";
			System.out.println(messageSqlQuery);
			
			Connection conn = null;
			PreparedStatement stmt = null;
			
			try{
				// Register JDBC Driver
				Class.forName("com.mysql.jdbc.Driver");
				
				// Open a connection
				System.out.println("Connecting to database.....");
				conn = DBCon.getRemoteConnection();
				
				// EXECUTE A QUERY 
				System.out.println("Creating a statement");
				stmt = conn.prepareStatement(messageSqlQuery, Statement.RETURN_GENERATED_KEYS);
				stmt.executeUpdate();
				ResultSet rs = stmt.getGeneratedKeys();
				
				// Extract data from result set
				if (rs.next()){
					System.out.println("Delete messages success");
				} else {
					System.out.println("Delete messages fail");
				}
				
				// clean up connection
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
	}
	
	// returns a message object by its DBNotification
	public static Message getMessageByNotification(DBNotification notification){
		return getMessageByID(new Integer(notification.getEmailID()).toString());
	}
	
	// returns message object by its id in the database
	public static Message getMessageByID(String messageID){
		String sql = "SELECT * FROM message where message_id = '" + messageID + "';";
		return getMessageFromSQLStatement(sql);
	}
	
	// returns email struct by its id in the database 
	public static EmailStruct getEmailStructByID(int messageID){
		String sql = "SELECT * FROM message where message_id = " + messageID;
		return getMessageFromSQLStatement(sql).toEmailStruct();
	}
	
	// Returns a message object based on a valid sql query 
	public static Message getMessageFromSQLStatement(String sql){
		Connection conn = null;
		Statement stmt = null;
		Message message = null;
		
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
				String id = rs.getString("message_id");
				String messageSubject = rs.getString("message_subject");
				String messageContent = rs.getString("message_content");
				
				message = new Message(id, messageSubject, messageContent);
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
		return message;
	}
	
	
	public EmailStruct toEmailStruct(){
		EmailStruct emailStruct = SecMailServer.makeEmail();
		for (User recipient : recipients){
			emailStruct.addRecipient(recipient.getUserAddress());
		}
		emailStruct.setBody(content);
		emailStruct.setSubject(subject);
		emailStruct.setID(messageID);
		return emailStruct;
	}

}
