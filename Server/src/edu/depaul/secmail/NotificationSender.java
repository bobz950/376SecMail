/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;
//David Keller

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;


public class NotificationSender implements Runnable{
    private LinkedList <Notification> notifications;
    
    public NotificationSender( LinkedList <Notification> notifications ) {
        this.notifications = notifications;
    } 

	public void run(){
        for (Notification n: this.notifications){
            if (
            		SecMailServer.getGlobalConfig().getDomain() == n.getTo().getDomain() ||
            		n.getTo().getDomain() == "localhost" // for testing purposes.
        		){
                SecMailServer.addNotificationToList(n);
            }
            else
            {
                try {
	            	Socket remote = new Socket(n.getTo().getDomain(), n.getTo().getPort());
	                DHEncryptionIO io = new DHEncryptionIO(remote, false);
	                PacketHeader notificationHeader = new PacketHeader(Command.SEND_NOTIFICATION);
	
	                //Send NotificationHeader over Network
	                io.writeObject(notificationHeader);
	
	                //Send the Notification
	                io.writeObject(n);
	
	                io.writeObject(new PacketHeader(Command.CLOSE));
	                io.close();
	                remote.close();
                } catch (IOException e) {
                	Log.Error("Exception trying to send notificatio to user: "+n.getTo().compile());
                	Log.Error(e.toString());
                }
            }
        }
    }
}