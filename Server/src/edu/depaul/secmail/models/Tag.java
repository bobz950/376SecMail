// Bianca Cote

package edu.depaul.secmail.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.depaul.secmail.DBCon;

public class Tag implements DBModel {
	
	private int tagID;
	private final String tagName;
	
	public Tag(int tagID, String tagName){
		this.tagID = tagID;
		this.tagName = tagName;
	}
	
	public Tag(String tagName){
		this.tagName = tagName;
	}
	
	@Override
	public String toString() {
		return "Tag [tagID=" + tagID + ", tagName=" + tagName + "]";
	}

	public String getTagName(){
		return tagName;
	}
	
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return tagID;
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
		/// Only write to the database if the tag isn't already stored in it 
		if (tagID == 0){
			
			String sql = "INSERT INTO tag VALUES (0,  \"" + tagName + "\")";
			
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
					System.out.println("Insert Tag Success");
					tagID = rs.getInt(1);
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

}
