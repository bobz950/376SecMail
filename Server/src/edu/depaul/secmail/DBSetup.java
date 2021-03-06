// Caylah Peoples
// The AWS instance is a subject to change. Replace the hostname with the aws rds endpoint, and the appropriate username and password

package edu.depaul.secmail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DBSetup {
	
	public static void main(String[] args){
		createTables();
	}

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	private static Connection getRemoteConnection() {
	    if (true) {
	      try {
	      Class.forName(JDBC_DRIVER);
	      String dbName = "ebdb";
	      String userName = "secmail";
	      String password = "secmailserver";
	      String hostname = "aa8k61tu29nkx5.c2hedgdap9po.us-east-2.rds.amazonaws.com";
	      String port = "3306";
	      String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
	      System.out.println("Getting remote connection with connection string from environment variables.");
	      Connection con = DriverManager.getConnection(jdbcUrl);
	      System.out.println("Remote connection successful.");
	      System.out.println("connection successful");
	      return con;
	    }
	    catch (ClassNotFoundException e) { 
	    	System.out.println("Classnotfoundexception");
	    	//logger.warn(e.toString());
	    	}
	    catch (SQLException e) { 
	    	//logger.warn(e.toString());
	    	System.out.println("sql exception");
	    	System.out.println(e.toString());
	    	}
	    }
	    System.out.println("RDS_Hostname = null");
	    return null;
	}
	
	
	private static void createTables(){
		PreparedStatement stmt = null;
		Connection conn = getRemoteConnection();
		
		// Table Queries
		String createUser = "CREATE TABLE user(user_id INT AUTO_INCREMENT, user_address VARCHAR(40) NOT NULL UNIQUE, user_password VARCHAR(40) NOT NULL, user_salt VARCHAR(40) , primary key(user_id));";
		String createMessage = "CREATE TABLE message(message_id INT AUTO_INCREMENT, message_subject VARCHAR(100),message_content VARCHAR(1000),message_attatchment BLOB, PRIMARY KEY (message_id));";
		String createTag = "CREATE TABLE tag(tag_id INT auto_increment,tag_name VARCHAR(40),primary key ( tag_id));";
		String createMessageTag = "CREATE TABLE message_tag(tag_id INT,message_id INT, recipient_id INT,FOREIGN KEY (tag_id) REFERENCES tag(tag_id),FOREIGN KEY (recipient_id) REFERENCES user(user_id));";
		String createNotification = "CREATE TABLE notification(notification_id INT NOT NULL AUTO_INCREMENT, sender_id INT NOT NULL, recipient_id INT NOT NULL, message_id INT, message_date DATETIME, PRIMARY KEY (notification_id), FOREIGN KEY (sender_id) REFERENCES user (user_id),FOREIGN KEY (recipient_id) REFERENCES user (user_id), FOREIGN KEY (message_id) REFERENCES message (message_id));";
		
		
		// Execute Queries
		try{
			
			// Create User Table
			stmt = conn.prepareStatement(createUser);
			int rs = stmt.executeUpdate();
			if (rs != 0){
				System.out.println("Create User table success");
			} else {
				System.out.println("Create User table fail");
			}
			
			// Create Message Table
			stmt = conn.prepareStatement(createMessage);
			rs = stmt.executeUpdate();
			if (rs != 0){
				System.out.println("Create Message table success");
			} else {
				System.out.println("Create Message table fail");
			}
			
			// Create Tag Table
			stmt = conn.prepareStatement(createTag);
			rs = stmt.executeUpdate();
			if (rs != 0){
				System.out.println("Create Tag table success");
			} else {
				System.out.println("Create Tag table fail");
			}
			
			// Create MessageTag Table
			stmt = conn.prepareStatement(createMessageTag);
			rs = stmt.executeUpdate();
			if (rs != 0){
				System.out.println("Create MessageTag table success");
			} else {
				System.out.println("Create MessageTag table fail");
			}
			
			// Create MessageRecipient Table
			stmt = conn.prepareStatement(createNotification);
			rs = stmt.executeUpdate();
			if (rs != 0){
				System.out.println("Create MessageRecipient table success");
			} else {
				System.out.println("Create MessageRecipient table fail");
			}
			
			// Clean up connection
			stmt.close();
			conn.close();
			
		}catch(SQLException se){
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
