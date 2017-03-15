// Alan Strimbu

package edu.depaul.secmail.models;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;

import edu.depaul.secmail.DBCon;
import edu.depaul.secmail.SecMailServer;
import edu.depaul.secmail.UserStruct;

public class User implements DBModel {

	private int userID;
	private String userAddress;
	private String userPassword;
	private String userSalt;
	
	// Constructor for instantiating User object from the DB, where it already has a userID
	public User(int userID, String userAddress, String userPassword){
		this.userID = userID;
		this.userAddress = userAddress;
		this.userPassword =userPassword;
		try {
			setSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	// Constructor for creating a user object to write to the DB
	public User(String userAddress, String userPassword){
		this.userAddress = userAddress;
		this.userPassword =userPassword;
		try {
			setSalt();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		hashPassword();
	}
	
	public User(String user) {
		this.userAddress = user;
		this.userPassword = null;
		this.userSalt = null;
		this.userID = getUserIDFromDB(user);
	}

	public String getUserAddress() {
		return userAddress;
	}

	public String getUserSalt() {
		return userSalt;
	}

	@Override
	public int getID() {
		return userID;
	}

	@Override
	public String toString() {
		return "User [userID=" + userID + ", userAddress=" + userAddress + ", userPassword=" + userPassword
				+ ", userSalt=" + userSalt + "]";
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
		
		// Only write to the database if the user isn't already stored in it 
		if (userID == 0){
			
			String sql = "INSERT INTO user VALUES (0,  \""+userAddress + "\", \"" + userPassword + "\", \"" + userSalt + "\")";
			
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
				
				// set userID from newly inserted row
				if (rs.next()){
					System.out.println("Insert User Success");
					userID = rs.getInt(1);
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
	}
	
	// returns a User object from a userID
	public static User getUserFromID(int userID){
		String sql = "SELECT * FROM user where user_id = " + userID;
		return getUserFromSQLStatement(sql);
	}
	
	// returns a User object froma userAdress
	// userAddress guarenteed to be unique, enforced by the db
	public static User getUserFromAddress(String userAddress){
		String sql = "SELECT * FROM user where user_address = \"" + userAddress + "\"";
		return getUserFromSQLStatement(sql);
	}
	
	// returns a UserStruct from a userID
	public static UserStruct getUserStructFromID(int userID){
		return getUserFromID(userID).toUserStruct();
	}
	
	// returns a UserStruct from a userAddress
	public static UserStruct getUserStructFromAddress(String userAddress){
		return getUserFromAddress(userAddress).toUserStruct();
	}
	
	public static User getUserFromSQLStatement(String sql){
		Connection conn = null;
		Statement stmt = null;
		User user = null;
		
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
				int id = rs.getInt("user_id");
				String userAddress= rs.getString("user_address");
				String userPassword = rs.getString("user_password");
				user = new User(id, userAddress, userPassword);
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
		return user;
	}
	

	
	// Generates a salt and saves it 
	private void setSalt() throws NoSuchAlgorithmException{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		userSalt = new BigInteger(salt).toString();
	}
	
	// hashes the current password
	private void hashPassword(){
		String generatedPassword = null;
		try{
			MessageDigest md =MessageDigest.getInstance("MD5");
			md.update(new BigInteger(userSalt).toByteArray());
			byte[] passwordBytes = md.digest(userPassword.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i =0; i < passwordBytes.length; i++){
				sb.append(Integer.toString((passwordBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
			userPassword = generatedPassword;
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}
	
	public static String hashPassword(String passwordToHash, byte[] salt){
		String generatedPassword = null;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			byte[] bytes = md.digest(passwordToHash.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++){
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return generatedPassword;
	}
	
	private static byte[] getStoredSalt(String username) {
		String query = "SELECT user_salt FROM user WHERE user_address=?;";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBCon.getRemoteConnection();
			stmt = conn.prepareStatement(query);
			stmt.setString(1, username);
			
			ResultSet r = stmt.executeQuery();
			String bytestring = "";
			if (r.next()) bytestring = r.getString("user_salt");
			else return null;
			byte[] salt = new BigInteger(bytestring).toByteArray();
			return salt;
		} catch (SQLException e) {
			return null;
		}
	}
	
	public static boolean authenticate(String username, String password) {
		byte[] salt = getStoredSalt(username);
		if (salt == null) {
			System.out.println("Couldnt get salt");
			return false;
		}
		User u = getUserFromAddress(username);
		String attemptedPass = hashPassword(password, salt);
		if (attemptedPass.equals(u.userPassword)) return true;
		else {
			System.out.println("Failed login attempt");
			return false;
		}
	}
	
	public static int getUserIDFromDB(String username) {
		String query = "SELECT user_id FROM user WHERE user_address=?;";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBCon.getRemoteConnection();
			stmt = conn.prepareStatement(query);
			stmt.setString(1, username);
			
			ResultSet r = stmt.executeQuery();
			int id = 0;
			if (r.next()) id = r.getInt("user_id");
			return id;
		} catch (SQLException e) {
			return 0;
		}
	}


	// returns a userStruct object
	public UserStruct toUserStruct(){
		return SecMailServer.makeUser(userAddress + "@" + SecMailServer.getGlobalConfig().getDomain());
	}
	
//	public static void main(String[] args) {
//		User u = new User("test2","test");
//		u.dbWrite();
//	}
}