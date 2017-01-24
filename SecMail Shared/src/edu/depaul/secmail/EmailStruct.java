/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.util.Arrays;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.math.BigInteger;

public class EmailStruct implements Serializable{
	private LinkedList<UserStruct> recipients = new LinkedList<UserStruct>();
	private transient LinkedList<File> attachments = new LinkedList<File>();
	private String subject = null;
	private String body = null;
	private String id = null;
	private byte[] encryptedBytes = null;
	private boolean encrypted = false;
	private transient FileInputStream fis;
	private transient FileOutputStream fos;
	
	//default empty constructor.
	EmailStruct()
	{
		
	}
	
	//Constructor for reading an email from a file.
	EmailStruct(File f)
	{		
		try {
			fis = new FileInputStream(f);
			String line = null;
			while ((line = custom_readLine()) != null)
			{
				if (line.startsWith("to:"))
				{
					//add a recipient
					String[] split = line.split(":");
					if (split.length > 2)
						 fileFormatError(line);
					else
						recipients.add(new UserStruct(split[1].trim()));
				}
				else if (line.startsWith("attachment:"))
				{
					//add attachment
					String[] split = line.split(":");
					if (split.length > 3) // windows will have at least 2 ':'
						fileFormatError(line);
					else
					{
						String path;
						if (split.length > 2)
							path = split[1] + ":" + split[2];
						else
							path = split[1];
						File attachment = new File(path.trim());
						attachments.add(attachment);
					}
				}
				else if (line.startsWith("subject:"))
				{
					//add the subject
					//String[] split = line.split(":");
					int idx = line.indexOf(':');
					this.subject = line.substring(idx+1).trim();
				}
				else if (line.startsWith("body:"))
				{
					//add the body
					StringBuffer buffer = new StringBuffer();
					//consume the rest of the file
					while ((line = custom_readLine()) != null)
						buffer.append(line);
					body = buffer.toString();
				}
				else if (line.startsWith("Encrypted:"))
				{
					encrypted = true;
					
					//consume the rest of the file
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					int nextByte;
					while ((nextByte = fis.read()) != -1)
						bos.write((byte)nextByte);
					encryptedBytes = bos.toByteArray();
				}
				else
				{
					fileFormatError(line);
				}
			}
			fis.close();
		} catch (Exception e)
		{
			System.out.println(e);
		}
	}
	
	//reads the email from file f, and sets the id to ID
	EmailStruct(File f, String ID)
	{
		this(f);
		this.id = ID;
	}	
	
	//helper function. Simply writes an error message to stdout.
	private void fileFormatError(String line)
	{
		System.out.println("Format error reading email from file. offending line:");
		System.out.println(line);
	}
	
	//add the user denoted by the string to the recipients list
	//creates the appropriate UserStruct from the incoming string first.
	public void addRecipient(String to)
	{
		System.out.println("Added to:" + to);
		recipients.add(new UserStruct(to));
	}
	
	//Add the File attachment to the list of attachments for this email
	public void addAttachment(File attachment)
	{
		if (attachments == null)
			attachments = new LinkedList<File>();
		attachments.add(attachment);
	}
	
	//set the subject of the email
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	
	//set the body of the email
	public void setBody(String body)
	{
		this.body = body;
	}
	
	//Generates a single string with comma separated entries for each user in the recipients list
	public String getToString()
	{
		StringBuffer buffer = new StringBuffer();
		for (UserStruct recipient : recipients)
			buffer.append(recipient.compile() + ",");
		buffer.setLength(buffer.length() - 1); // delete the last character
		return buffer.toString();
	}
	
	//returns the list of attachments for this email
	public LinkedList<File> getAttachmentList()
	{
		return attachments;
	}
	
	//returns the subject of the email
	public String getSubject()
	{
		return subject;
	}
	
	//returns the body of the email
	public String getBody()
	{
		return body;
	}
	
	//writes the contents of this email message to a file f
	//returns true if successful, returns false otherwise
	public boolean writeToFile(File f)
	{
		try {
			fos = new FileOutputStream(f);
			//write the recipients
			for (UserStruct recipient : recipients)
			{
				custom_write("to: ");
				custom_writeLine(recipient.compile());
			}
			
			//write the attachments
			if (attachments != null)
				for (File attachment : attachments)
				{
					custom_write("attachment: ");
					custom_writeLine(attachment.getAbsolutePath());
				}
			
			//write the subject
			custom_write("subject: ");
			custom_writeLine(subject);
			
			//the rest of the email is the body.
			if (encrypted)
			{
				custom_writeLine("Encrypted:");
				fos.write(encryptedBytes);
			}
			else
			{	
				custom_writeLine("body: ");
				custom_write(body);
			}
				
			
			fos.close();
			return true;
		} catch (Exception e)
		{
			System.out.println(e);
			return false;
		}
			
	}
	
	//get the unique ID for this email. Generates the ID if the email doesn't already have one.
	public String getID()
	{
		if (this.id == null)
		{
			//generate a random id string
			this.id = new BigInteger(130, new SecureRandom()).toString(32);
		}
		
		return id;
	}
	
	//return the entire list of recipients
	public LinkedList<UserStruct> getToList()
	{
		return recipients;
	}
	
	//returns a list of notifications of type NEW_EMAIL appropriate for this email
	public LinkedList<Notification> getNotificationList(UserStruct fromUser)
	{
		LinkedList<Notification> ret = new LinkedList<Notification>();
		for (UserStruct recipient : recipients)
		{
			ret.add(new Notification(recipient, fromUser, NotificationType.NEW_EMAIL, this));
		}
		return ret;
	}
	
	//encrypts the body of this EmailStruct
	public void encrypt(String pass)
	{
		try {
			byte[] key = pass.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // use only first 128 bit
			if (body != null && !encrypted)
			{
				encryptedBytes = SecMailStaticEncryption.SecMailEncryptAES(body, key); // encrypt the body using the key
				body = null; // erase the body
				encrypted = true; // mark that we're encrypted
			}
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println("Unable to encrypt email.");
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}
	
	//decrypts an already-encrypted body
	//returns true if successful or false if not
	public boolean decrypt(String pass)
	{
		try {
			byte[] key = pass.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // use only first 128 bit
			if (encrypted)
			{
				body = SecMailStaticEncryption.SecMailDecryptAES(encryptedBytes, key);
				encryptedBytes = null; // delete the encrypted portion
				encrypted = false; // set that we're no longer encrypted.
				return true; //TODO: make this actually check to see if decryption was successful.
			}
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println("Unable to encrypt email.");
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isEncrypted()
	{
		return encrypted;
	}
	
	//returns whether the email has attachments in the attachment list
	public boolean hasAttachments()
	{
		if (attachments == null)
			return false;
		else return !attachments.isEmpty();
	}
	
	//custom method to read a line of characters from the input file.
	//returns null on EOF or exception
	private String custom_readLine()
	{
		final int newline = 10;
		StringBuffer sb = new StringBuffer();
		int nextByte;
		try {
			while ((nextByte = fis.read()) != newline)
	        {
	          if (nextByte == -1) //if EOF
	            break;
	
	          sb.append((char)nextByte);
	        }
		} catch (IOException e) 
		{
			//e.printStackTrace();
			sb.setLength(0);
		}
		
		if (sb.length() == 0)
			return null;
		else
			return sb.toString();
	}
	
	private void custom_writeLine(String toWrite)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bos);
		writer.println(toWrite);
		writer.flush();
		try {
			fos.write(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void custom_write(String toWrite)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(bos);
		writer.print(toWrite);
		writer.flush();
		try {
			fos.write(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
