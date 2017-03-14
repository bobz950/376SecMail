package edu.depaul.secmail.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class User implements DBModel {

	private int userID;
	private String userAddress;
	private String userPassword;
	private String userSalt;
	
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
	
	private void setSalt() throws NoSuchAlgorithmException{
		SecureRandom sr = SecureRandom.getInstance("SHAPRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		userSalt = salt.toString();
	}
	
	private void hashPassword(){
		String generatedPassword = null;
		try{
			MessageDigest md =MessageDigest.getInstance("MD5");
			md.update(userSalt.getBytes());
			byte[] passwordBytes = md.digest(userPassword.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i =0; i < passwordBytes.length; i++){
				sb.append(Integer.toString((passwordBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		
	}

}