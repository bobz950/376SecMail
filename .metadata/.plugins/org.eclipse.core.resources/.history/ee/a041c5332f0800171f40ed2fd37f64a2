package edu.depaul.secmail.models;

public class User implements DBModel {

	private final int userID;
	private final String userAddress;
	private final String userPassword;
	private final String userSalt;
	
	public User(int userID, String userAddress, String userPassword, String userSalt){
		this.userID = userID;
		this.userAddress = userAddress;
		this.userPassword =userPassword;
		this.userSalt = userSalt;
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
		
		
	}

}
