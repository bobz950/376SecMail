package edu.depaul.secmail;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpSession {
	//Robert Alianello
	private static Map<String, HashMap<String, String>> sessions = new HashMap<String, HashMap<String, String>>(); //collection of all active sessions
	private static Map<String, Long> sessionLog = new HashMap<String, Long>();
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
	public static HashMap<String, String> get(String id) {
		//get session variables for specified session id
		return sessions.get(id);
	}
	//Robert Alianello
	public static String generateSessionID() {
		//generates unique session ID to be given to the client within a cookie
		SecureRandom r = new SecureRandom();
		return new BigInteger(130, r).toString(32);
	}
	//Robert Alianello
	public static String start(String username) {
		//generates new session id, adds to sessions collection, returns session id to client
		String id = generateSessionID();
		Map<String, String> newUserSession = new HashMap<String, String>();
		newUserSession.put("user", username);
		sessions.put(id, ((HashMap<String, String>)newUserSession));
		sessionLog.put(id, new Date().getTime());
		return id;
	}
	//Robert Alianello
	public static void updateTime(String id) {
		if (sessionLog.containsKey(id)) sessionLog.put(id, new Date().getTime());
	}
	//Robert Alianello
	public static boolean isSet(String id) {
		//checks if a session is active for userid
		if (sessions.containsKey(id)) return true;
		return false;
	}
	//Robert Alianello
	public static synchronized void remove(String id) {
		sessions.remove(id);
		sessionLog.remove(id);
	}
	
	

}
