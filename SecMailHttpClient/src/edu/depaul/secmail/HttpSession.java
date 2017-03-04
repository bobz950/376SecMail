package edu.depaul.secmail;

import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpSession {
	//Robert Alianello
	protected static Map<String, Runnable> sessions = new HashMap<String, Runnable>(); //collection of all active sessions
	private static Map<String, Long> sessionLog = new HashMap<String, Long>();
	private static Object sessionLock = new Object(); //object used to lock access to both collections. Ensure only one thread at a time can modify both maps
	//Robert Alianello
	public static class sessionCleaner implements Runnable {
		//Executes a session clean every 60 seconds which will remove inactive sessions from collection
		public void run() {
			while (true) {
				try {
					Thread.sleep(60000);
					this.sessionClean();
				} 
				catch (InterruptedException e) {} //TO DO -- figure out what to do here
			}
		}
		//Robert Alianello
		public void sessionClean() {
			Iterator<Entry<String, Long>> it = sessionLog.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Long> p = it.next();
				Long entryTime = p.getValue();
				String key = p.getKey();
				
				//if more than 10 minutes since sessionID was last used, kill session
				if ((new Date().getTime() - entryTime) > 600000) {
					HttpSession.remove(key);
				}
			}
		}
	}
	
	//Robert Alianello
	public static MailServerConnection get(String id) {
		//get session variables for specified session id
		return (MailServerConnection)sessions.get(id);
	}
	//Robert Alianello
	public static String generateSessionID() {
		//generates unique session ID to be given to the client within a cookie
		SecureRandom r = new SecureRandom();
		return new BigInteger(130, r).toString(32);
	}
	//Robert Alianello
	public static String start(String username, Socket s, DHEncryptionIO io) {
		//generates new session id, adds to sessions collection, returns session id to client
		String id = generateSessionID();
		try {
			Thread newUserSession = new MailServerConnection(id, username, s, io);
			newUserSession.start();
			//ensure thread safe
			synchronized(sessionLock) {
				sessions.put(id, newUserSession);
				sessionLog.put(id, new Date().getTime());
			}
			
		}
		catch (Exception e) { return null; }
		return id;
	}
	//Robert Alianello
	public static void updateTime(String id) {
		if (sessionLog.containsKey(id)) sessionLog.put(id, new Date().getTime());
	}
	//Robert Alianello
	public static boolean isSet(String id) {
		//checks if a session is active for userid
		if (id == null) return false;
		if (sessions.containsKey(id)) return true;
		return false;
	}
	//Robert Alianello
	public static void remove(String id) {
		//make sure connections get closed
		((MailServerConnection)sessions.get(id)).close();
		//needs to be synchronized so multiple threads can execute without damaging the data
		synchronized(sessionLock) {
			//remove from session and session log
			sessions.remove(id);
			sessionLog.remove(id);
		}
	}
	
	

}
