package edu.depaul.secmail;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.LinkedList;

//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.MouseAdapter;
//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.FormAttachment;
//import org.eclipse.swt.layout.FormData;
//import org.eclipse.swt.layout.FormLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.MessageBox;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MailServerConnection extends Thread {
	private Socket s;
	private DHEncryptionIO secIO;
	private String sessionID;
	private String user;
	
	public MailServerConnection(String session, String user) {
		this.user = user;
		this.sessionID = session;
	}
	
	
	public void run() {
		try {
			this.s = new Socket(Main.host, Main.port);
			this.secIO = new DHEncryptionIO(s, false);
		} 
		catch (UnknownHostException e) { System.out.println("Could not connect to host"); } 
		catch (IOException e) { System.out.println("IOException while creating input/output stream"); }
	}
	
	public void close() {
		try {
			secIO.writeObject(new PacketHeader(Command.CLOSE));
			secIO.close();
			s.close();
		} 
		catch (IOException e) { return; }
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getSessionID() {
		return this.sessionID;
	}
	
	//This is where we add methods for making requests for data from the main server
	
	
	
		//Rohail Baig	
//        private void OpenOrFetchMail(TableItem item) throws ClassNotFoundException, IOException {
//        	//enables recieving Notifications.
//    		Notification n = (Notification)item.getData();
//    		//get mail from local system
//    		
//    		File f = new File(MainWindow.getMailDir() + n.getID());
//    		
//    		//make packet header to send to client
//			PacketHeader getEmailHeader = new PacketHeader(Command.RECIEVE_EMAIL);
//			//send packet header to server
//			io.writeObject(getEmailHeader);
//			//send ID to Client connected to server 
//			io.writeObject(n.getID());
//			//send from user to server for email receipt
//			io.writeObject(n.getFrom());
//			
//			PacketHeader responsePacket = (PacketHeader) io.readObject();
//			if(responsePacket.getCommand() == Command.RECIEVE_EMAIL){
//				//server sends back the email / packet header
//				EmailStruct email = (EmailStruct)io.readObject();
//				email.writeToFile(f);
//				item.setText(3, "Yes"); // update the table item to show that the email has been downloaded
//				
//				//open in mail reader
//				EmailReader reader = new EmailReader(Display.getCurrent(), email, n.getFrom(), n.getDate(), io);
//				reader.open();			
//			}
//			else{
//				noEmailOnServer();
//			}
//			}
//		}
//       
//	private void noNotificationsMessageBox(){
//		
//		Shell noNotifications = new Shell();
//		MessageBox messageBox = new MessageBox(noNotifications, SWT.OK);
//		messageBox.setText("No Notifications");
//		messageBox.setMessage("You have no notifications!");		
//		messageBox.open();
//	}
//	
//	//Josh Clark
//	private void noEmailOnServer(){
//		
//		Shell noEmail = new Shell();
//		MessageBox messageBox = new MessageBox(noEmail, SWT.OK);
//		messageBox.setMessage("Email is no longer on server, Need to restart connection.");		
//		messageBox.open();
//	}
        
        
	//Rohail Baig
		//Searching through emails.	
		public void handleSearchByKey(String oldVal, String newVal) {
			// If the number of characters in the text box is less than last search entry
			// it must be because the user pressed delete
			if ( oldVal != null && (newVal.length() < oldVal.length()) ) {
				// Restore the lists original set of entries, and start from the beginning
//				list.setItems(entries);
				}
				     
			 // Break out all of the parts of the search text by splitting on white space 
			 
			String[] parts = newVal.toUpperCase().split(" ");
				 
				    // Filter out the entries which do not contain the entered text
			ObservableList<String> subentries = FXCollections.observableArrayList();
			for ( Object entry: list.getItems() ) {
				boolean match = true;
				String entryText = (String)entry;
				for ( String part: parts ) {
				    // The entry needs to contain all portions of the search string in any order
				    if ( ! entryText.toUpperCase().contains(part) ) {
				    	match = false;
				    	break;
				    	}
				    }
				 
				        if ( match ) {
				            subentries.add(entryText);
				        }
				    }
				    //list.setItems(subentries);
				}
}
