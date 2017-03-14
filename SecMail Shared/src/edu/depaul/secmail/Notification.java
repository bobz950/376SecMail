/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable{
	private UserStruct from; // the user who sent the notification
	private UserStruct to; // the user for whom the notification is destined to reach
	private NotificationType type; // the type of notification
	private String id; // the id of the email that the notification refers to
	private String subject; // the subject of the email that the notification refers to
	private Date sendDate; // date that this notification was sent.
	
	//default constructor. Only sets the date field.
	Notification()
	{
		sendDate = new Date();
	}
	
	//take a full to user (ex: jacob@burkamper.com:1234), a full from user, a type for the notification, and an email.
	// turn them into a notification
	Notification(String fullUser, String fullFrom, NotificationType type, EmailStruct e)
	{
		this();
		this.from = new UserStruct(fullFrom);
		this.to= new UserStruct(fullUser);
		this.type = type;
		this.id = e.getID();
		this.subject = e.getSubject();
	}
	
	//Take a UserStruct for to, from, the Notification type, and an email. Assign the appropriate values based on this input
	Notification(UserStruct toUser, UserStruct fromUser, NotificationType type, EmailStruct e)
	{
		this();
		this.from = fromUser;
		this.to = toUser;
		this.type = type;
		this.id = e.getID();
		this.subject = e.getSubject();
	}
	
	//Take each field directly, including Date. Most likely only really useful for testing...
	Notification(UserStruct toUser, UserStruct fromUser, NotificationType type, String id, String subject, Date date)
	{
		this.from = fromUser;
		this.to= toUser;
		this.type = type;
		this.id = id;
		this.subject = subject;
		this.sendDate = date;
	}
	
	//get a UserStruct which represents the intended recipient of the mail which the notification refers to
	public UserStruct getTo()
	{
		return this.to;
	}
	
	//get a UserStruct which represents the sender of the mail which the notification refers to
	public UserStruct getFrom()
	{
		return this.from;
	}
	
	//get the type of the notification
	public NotificationType getType()
	{
		return this.type;
	}
	
	//get the id of the email that the notification refers to
	public String getID()
	{
		return this.id;
	}
	
	//get the subject line of the email that the notification refers to.
	public String getSubject()
	{
		return subject;
	}
	
	//get the Date object from the notification
	public Date getDate()
	{
		return sendDate;
	}
	public String toString(){
		return "to: "+this.to.compile() +
				"\nfrom: "+this.from.compile() +
				"\nsubject "+this.subject;
	}
	
	
}
