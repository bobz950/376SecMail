// Caylah Peoples

package edu.depaul.secmail;

import java.sql.*;
import java.util.ArrayList;

import edu.depaul.secmail.models.DBNotification;
import edu.depaul.secmail.models.Message;
import edu.depaul.secmail.models.Tag;
import edu.depaul.secmail.models.User;

public class DBCon {
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String dbName = "ebdb";
    static final String userName = "secmail";
    static final String password = "secmailserver";
    static final String hostname = "aa8k61tu29nkx5.c2hedgdap9po.us-east-2.rds.amazonaws.com";
    static final String port = "3306";
    static final String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
	
	public static void main(String[] args){
		//addUser("Will", "password");
		//getAllUsers();
		//insertMessage("1", "1", "Hello Subject", "Hello Content",  "null");
		//insertTag("SecMail");
		//getAllMessages();
		//getRemoteConnection();
		
		
		//User x = new User("Usernamekljh", "Password");
		//System.out.println(x.toString());
		//x.dbWrite();
		//System.out.println(x.toString());
		
		//User x2 = new User("Username5", "Password");
		//System.out.println(x.toString());
		//x2.dbWrite();
		//System.out.println(x.toString());
		
		//User x3 = new User("Username3", "Password");
		//System.out.println(x.toString());
		//x3.dbWrite();
		//System.out.println(x.toString());
		
		//User x4 = new User("Username4", "Password");
		//System.out.println(x.toString());
		//x4.dbWrite();
		//System.out.println(x.toString());
		
		//User x = User.getUserFromID(1);
		//System.out.println(x);
		//System.out.println(User.getUserFromAddress("Username"));
		
		//System.out.println(x2);
		//System.out.println(User.getUserFromAddress("Username2"));
		
		//System.out.println(User.getUserFromID(3));
		//System.out.println(User.getUserFromAddress("Username3"));
		
		//System.out.println(User.getUserFromID(4));
		//System.out.println(User.getUserFromAddress("Username4"));
	
		//Message m = new Message(x, x, "Subject", "Content", new Date(0));
		//System.out.println(m.toString());
		//m.addRecipient(x3);
		//m.addRecipient(x4);
		//m.dbWrite();
		//System.out.println(m.toString());
		
		//Tag t = new Tag("tag");
		//System.out.println(t.toString());
		//t.dbWrite();
		//System.out.println(t.toString());
		
		//DBNotification dbn = new DBNotification(x, x, "subject", m.getID(), new Date(0));
		//System.out.println(dbn.toString());
		//dbn.dbWrite();
		//System.out.println(dbn.toString());
		

		
		System.out.println(Message.getMessageByID("os25l8l6nd57fsugsvmkts1h9d"));
		//ArrayList<DBNotification> notifications = DBNotification.getDBNotificationsByRecipientID("13");
		//for (DBNotification n : notifications){
			//System.out.println("Notification: ");
			//System.out.println(n + "\n");
			//System.out.println("Messsage: ");
			//System.out.println(n.getMessageByDBNotification() + "\n");
		//}
	
	}
	
	// Return Connection to AWS Server
	public static Connection getRemoteConnection() {
	    if (true) {
	      try {
	      Class.forName(JDBC_DRIVER);
	      
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
	
	
	
	
	
	
	public static void addUser(String username, String password){

		String sql = "INSERT INTO user VALUES (0,  \""+username + "\", \"" + password + "\", null)";
		insertSql(sql);
	}
	
	public static void getAllUsers(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM user";
			ResultSet rs = stmt.executeQuery(sql);
			
			// Extract data from result set
			while (rs.next()){
				int id = rs.getInt("user_id");
				String userAddress= rs.getString("user_address");
				String userPassword = rs.getString("user_password");
				User user = new User(id, userAddress, userPassword);
				
				System.out.println(user.toString());
				
				//System.out.println("*****************************");
				//System.out.println("User ID: " + user.getID());
				//System.out.println("User Address: " + user.getUserAddress());
				//System.out.println("*****************************");
				//System.out.println();;
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
	}
	
	public static void getAllMessages(){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM message";
			ResultSet rs = stmt.executeQuery(sql);
			
			// Extract data from result set
			while (rs.next()){
				int id = rs.getInt("message_id");
				String address= rs.getString("sender_id");
				String messageID = rs.getString("message_id");
				String subject = rs.getString("message_subject");
				String content = rs.getString("message_content");
			
				System.out.println("********************************");
				System.out.println("Message ID: " + messageID);
				System.out.println("User ID: " + id);
				System.out.println("User Address: " + address);
				System.out.println("Message subject: " + subject);
				System.out.println("Message content: " + content);
				System.out.println("********************************\n");
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
	}
	
	
	public static void insertSql(String sqlQuery){
		Connection conn = null;
		Statement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.createStatement();

			int rs = stmt.executeUpdate(sqlQuery);
			
			// Extract data from result set
			if (rs != 0){
				System.out.println("Insert success");
			} else {
				System.out.println("Insert fail");
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
	
	
	public static void insertMessage(String sender, String recipient, String subject, String content, String date){
		String messageSqlQuery = "INSERT INTO message VALUES (0,  "+sender + ", \"" + subject + "\", + \""+content + "\", + \"null\", + null)";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try{
			// Register JDBC Driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database.....");
			conn = getRemoteConnection();
			
			// EXECUTE A QUERY 
			System.out.println("Creating a statement");
			stmt = conn.prepareStatement(messageSqlQuery, Statement.RETURN_GENERATED_KEYS);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			stmt.close();
			// Extract data from result set
			if (rs.next()){
				System.out.println("Insert message success");
				String messageRecipientSqlQuery = "INSERT INTO message_recipient VALUES ("+ recipient + ", " + rs.getInt(1) + ")";
				System.out.println(messageRecipientSqlQuery);
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
	
	public static void insertTag(String tag){
		String sqlQuery = "INSERT INTO tag VALUES (0,  \""+tag + "\")";
		insertSql(sqlQuery);
	}
	
}
