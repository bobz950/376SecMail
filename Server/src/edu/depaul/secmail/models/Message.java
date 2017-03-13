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

public class Message implements DBModel {

	private int messageID;
	private User sender;
	private User recipient;
	private String subject;
	private String content;
	// Message Attachment
	private Date messageDate;
	private ArrayList<Tag> tags;
	private boolean hasRead;
	
	
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
	
	public Message(User sender, User recipient, String subject, String content, Date messageDate){
		this.sender = sender;
		this.recipient = recipient;
		this.subject = subject;
		this.content = content;
		this.messageDate = messageDate;
	}

	public User getSender() {
		return sender;
	}

	public User getRecipient() {
		return recipient;
	}

	@Override
	public String toString() {
		return "Message [messageID=" + messageID + ", sender=" + sender + ", recipient=" + recipient + ", subject="
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
		String messageSqlQuery = "INSERT INTO message VALUES (0,  "+sender.getID() + ", \"" + subject + "\", + \""+content + "\", + \"null\", + null)";
		
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
				System.out.println("Insert message success");
				
				messageID = rs.getInt(1);
				
				// write into message recipient db table
				String messageRecipientSqlQuery = "INSERT INTO message_recipient VALUES ("+ recipient.getID() + ", " + rs.getInt(1) + ")";
				stmt = conn.prepareStatement(messageRecipientSqlQuery);
				int rsMR= stmt.executeUpdate();
				if (rsMR != 0){
					System.out.println("Insert message_recipient success");
				} else {
					System.out.println("Insert message_recipient fail");
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

}
