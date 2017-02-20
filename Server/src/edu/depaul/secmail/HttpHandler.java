package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

public class HttpHandler implements Runnable {
	public String response = "HTTP/1.1 200 OK\r\n"
			+ "Server: BobServer 1.0\r\n"
			+ "Content-Type: text/html\r\n"
			+ "Connection: Close\r\n";
	public Socket clientSock;
	public InputStream in;
	public OutputStream out;
	public HashMap<String, String> requestHeaders;
	public UserStruct user = null;
	public String method;
	
	public HttpHandler(Socket s) {
		this.clientSock = s;
	}
	
	public void setResponse(String length) {
		//add to response headers when request is for text
		SimpleDateFormat d = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
		String date = d.format(new Date());
		addToResponse("Date: " + date);
		addToResponse("Last-Modified: " + date);
		addToResponse("Content-Length: " + length);
	}
	
	public void setResponse() {
		//add to response headers when request is not for text
		SimpleDateFormat d = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
		String date = d.format(new Date());
		addToResponse("Date: " + date);
		addToResponse("Last-Modified: " + date);
	}
	
	public void run() {
		try {
			in = clientSock.getInputStream();
			out = clientSock.getOutputStream();
			BufferedReader b = new BufferedReader(new InputStreamReader(in));
			String requesttype;
			requesttype = b.readLine();
			//parse the request line
			String[] req = requesttype.split(" ");
			String method = req[0];
			this.method = method;
			String path = req[1];
			
			requestHeaders = parseHeaders(b);
			
			//if session is set, create new UserStruct and set to user
			String sessionID;
			if ((sessionID = checkSession(requestHeaders)) != null) {
				// check if sessionID is in sessions collection. Login user if true
				if (HttpSession.isSet(sessionID)) {
					this.user = new UserStruct(HttpSession.get(sessionID).get("user"), SecMailServer.getGlobalConfig().getDomain(), SecMailServer.getGlobalConfig().getPort());
					//update the time logged for this session ID so it stays active
					HttpSession.updateTime(sessionID);
				}
			}
			
			String outputBody = ""; //body that will be sent to client
			
			String acceptField = requestHeaders.get("Accept");
			//handle different request methods
			if (method.equals("GET")) {
				//handle GET requests for text
				if (acceptField.indexOf("text") >= 0) {
					outputBody = handleGetRequest(path);
					setResponse(new Integer(outputBody.length()).toString());
				}
				//to do : handle GET requests for images
			}
			else if (method.equals("POST")) {

				outputBody = handlePostRequest(path);
				setResponse(new Integer(outputBody.length()).toString());
			}
			else setResponse();
			
			out.write((response + "\r\n" + outputBody).getBytes());

		} 
		catch (IOException e) {
			System.out.println("IOException....");
		}
		try {
			clientSock.close();
		} 
		catch (IOException e) {
			System.out.println("IOException.... from thingy");
		}

		
	}
	
	public HashMap<String, String> parseHeaders(BufferedReader b) {
		String s;
		HashMap<String, String> parsed = new HashMap<String, String>();
		try {
			while ((s = b.readLine()).length() > 0) {
				String[] line = s.split(": ");
				if (line.length == 2) parsed.put(line[0], line[1]);
			}

			if (this.method.equals("POST")) {
				int num = Integer.parseInt(parsed.get("Content-Length"));
				int count = 0;
				char[] v = new char[num];
				while (num > 0) {
					v[count] = (char)b.read();
					num--;
					count++;
				}
				String vars = new String(v);
				String[] varArray = vars.split("&");
				
				for (int i = 0; i < varArray.length; i++) {
					String[] t = varArray[i].split("=");
					if (t.length > 1) parsed.put(t[0], t[1]);
				}
			}
		} 
		catch (IOException e) { }
		return parsed;
	}
	
	private String checkSession(HashMap<String, String> requestHeaders) {
		//check if session is active
		String cookie;
		if ((cookie = requestHeaders.get("Cookie")) != null) {
			String[] values = cookie.split("=");
			if (values.length < 2) return null;
			if (values[0].equals("SECMAILSESSIONID")) return values[1];
		}
		return null;
	}
	
	public void startSession(String username) {
		String sessionID = HttpSession.start(username);
		addToResponse("Set-Cookie: SECMAILSESSIONID=" + sessionID);
		
	}
	
	public void addToResponse(String s) {
		//add field to response header
		this.response = this.response + s + "\r\n";
	}
	
	private String handleGetRequest(String path) {
		String body = "<html><body><h1>Hello, World!</h1></body></html>";
		String msg;
		if (path.equals("/test")) msg = body.replaceAll("Hello", "Apple");
		if (path.equals("/test2")) {
			if (this.user != null) msg = "Logged in as: " + user.getUser();
			else msg = "Not logged in";
		}
		else if (path.equals("/login")) msg = "<form method='post' action='/signin'><input id='name' name='name' type='text'><input id='pass' name='pass' type='text'>><button>Login</button</form>";
		else msg = body;
		return msg;
	}
	
	private String handlePostRequest(String path) {
		String msg = "";
		if (path.equals("/signin")) {
			String name = this.requestHeaders.get("name");
			String pass = this.requestHeaders.get("pass");
			//msg = "Name: " + name + " Password: " + pass;
			if (handleLogin(name, pass)) msg = "Login Success!";
			else msg = "Invalid Username / Password";
		}
		return msg;
	}
	
	private boolean handleLogin(String username, String password) {
		Auth auth = new Auth();
		
		if (auth.login(username, password)) {
			//generate sessionID and store session. Add cookie to response
			startSession(username);
			return true;
		}
		else return false;
	}
	

}
