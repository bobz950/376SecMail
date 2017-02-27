package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

//Robert Alianello
public class HttpHandler implements Runnable {
	private String response = "HTTP/1.1 200 OK\r\n"
			+ "Server: SecMail HTTP Server 1.0\r\n"
			+ "Content-Type: text/html\r\n"
			+ "Connection: Close\r\n";
	private Socket clientSock;
	private InputStream in;
	private OutputStream out;
	private HashMap<String, String> requestHeaders;
	private UserStruct user = null;
	private RequestType method;
	private boolean isText = true;
	
	public enum RequestType {
		GET,
		POST,
		PUT,
		DELETE,
		OPTIONS,
		HEAD,
		TRACE,
		CONNECT,
		INVALID;
	}
	
	public HttpHandler(Socket s) {
		this.clientSock = s;
	}
	
	//Robert Alianello
	public void setResponse(String length) {
		//add to response headers when request is for text
		SimpleDateFormat d = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
		String date = d.format(new Date());
		addToResponse("Date: " + date);
		addToResponse("Last-Modified: " + date);
		addToResponse("Content-Length: " + length);
	}
	//Robert Alianello
	public void setResponse() {
		//add to response headers when request is not for text
		SimpleDateFormat d = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
		String date = d.format(new Date());
		addToResponse("Date: " + date);
		addToResponse("Last-Modified: " + date);
	}
	//Robert Alianello
	public RequestType setRequestType(String t) {
		if (t.equals("GET")) return RequestType.GET;
		else if (t.equals("POST")) return RequestType.POST;
		else if (t.equals("PUT")) return RequestType.PUT;
		else if (t.equals("DELETE")) return RequestType.DELETE;
		else return RequestType.INVALID;
	}
	//Robert Alianello
	public void run() {
		try {
			in = clientSock.getInputStream();
			out = clientSock.getOutputStream();
			BufferedReader b = new BufferedReader(new InputStreamReader(in));
			String requesttype;
			requesttype = b.readLine();
			//parse the request line
			String[] req = requesttype.split(" ");
			this.method = setRequestType(req[0]);
			String[] reqLocation = req[1].split("\\?");
			String path = reqLocation[0];
			
			requestHeaders = parseHeaders(b);
			//check if params added to request in URL and add to headers hashmap
			if (reqLocation.length > 1) addGetParams(requestHeaders, reqLocation); 
			
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
			if (method == RequestType.GET) {
				//handle GET requests for text
				if (acceptField.indexOf("text") >= 0) {
					outputBody = handleGetRequest(path);
					setResponse(new Integer(outputBody.length()).toString());
				}
				//handle GET requests for images
				else if (acceptField.indexOf("image") >= 0 ) {
					String fileType = path.split("\\.")[1];
					byte[] fileBytes = handleFileRequest(path);
					int filesize = fileBytes != null ? fileBytes.length : 0;
					byte[] headerBytes = new String("HTTP/1.1 200 OK\r\n"
							+ "Server: SecMail HTTP Server 1.0\r\n"
							+ "Content-Type: image/" + fileType + "\r\n"
							+ "Connection: Close\r\nContent-Length: " + filesize + "\r\n\r\n").getBytes();
					byte[] responseBytes = new byte[headerBytes.length + filesize];
					int i;
					for (i = 0; i < headerBytes.length; i++) responseBytes[i] = headerBytes[i];
					for (int j = 0; i < filesize; i++, j++) responseBytes[i] = fileBytes[j];
					out.write(responseBytes);
					isText = false;
					
				}
			}
			else if (method == RequestType.POST) {

				outputBody = handlePostRequest(path);
				setResponse(new Integer(outputBody.length()).toString());
			}
			else setResponse();
			
			//output response if the request was for text
			if (this.isText) out.write((response + "\r\n" + outputBody).getBytes());

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
	//Robert Alianello
	public HashMap<String, String> parseHeaders(BufferedReader b) {
		String s;
		HashMap<String, String> parsed = new HashMap<String, String>();
		try {
			while ((s = b.readLine()).length() > 0) {
				String[] line = s.split(": ");
				if (line.length == 2) parsed.put(line[0], line[1]);
			}

			if (this.method == RequestType.POST) {
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
		catch (IOException e) { }//TO DO -- figure out what to do here
		return parsed;
	}
	//Robert Alianello
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
	//Robert Alianello
	public void startSession(String username) {
		String sessionID = HttpSession.start(username);
		addToResponse("Set-Cookie: SECMAILSESSIONID=" + sessionID);
		
	}
	//Robert Alianello
	public void addToResponse(String s) {
		//add field to response header
		this.response = this.response + s + "\r\n";
	}
	//Robert Alianello
	private String handleGetRequest(String path) {
		String body = "<html><body><h1>Hello, World!</h1></body></html>";
		String msg = "";
		if (path.equals("/test")) msg = body.replaceAll("Hello", "Apple");
		else if (path.equals("/test2")) {
			if (this.user != null) msg = "Logged in as: " + user.getUser();
			else msg = "Not logged in";
		}
		else if (path.equals("/imgtest")) msg = "<b>Image: </b><br><img src='/pizza.png'>";
		else if (path.equals("/login")) msg = "<form method='post' action='/signin'><input id='name' name='name' type='text'><input id='pass' name='pass' type='text'>><button>Login</button</form>";
		else msg = body;
		return msg;
	}
	//Robert Alianello
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
	//Robert Alianello
	private byte[] handleFileRequest(String path) {
		byte[] bytes;
		try {
			File f = new File(path.substring(1));
			FileInputStream reader = new FileInputStream(f);
			bytes = new byte[(int)f.length()];
			reader.read(bytes);
			return bytes;
		} 
		catch (IOException e) {System.out.println("bad file");}//TO DO -- do something better here
		return null;
		
	}
	//Robert Alianello
	private boolean handleLogin(String username, String password) {
		Auth auth = new Auth();
		
		if (auth.login(username, password)) {
			//generate sessionID and store session. Add cookie to response
			startSession(username);
			return true;
		}
		else return false;
	}
	//Robert Alianello
	private void addGetParams(HashMap<String, String> rHeaders, String[] loc) {
		String[] params = loc[1].split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) rHeaders.put(p[0], p[1]);
		}
	}
	

}
