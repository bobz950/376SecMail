package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.depaul.secmail.content.*;

//Robert Alianello
public class HttpHandler implements Runnable {
	private String responseHeaders = "HTTP/1.1 200 OK\r\n"
			+ "Server: SecMail HTTP Server 1.0\r\n"
			+ "Connection: Close\r\n";
	private Socket clientSock;
	private InputStream in;
	private OutputStream out;
	private MailServerConnection mainConnection = null;
	private boolean validSession = false;
	private HashMap<String, String> requestHeaders; //stores request headers and request parameters
	private String user = null;
	private RequestType method;
	private boolean isText = true;
	//Robert Alianello
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
	public void setResponseHeaders(String length) {
		//add to response headers when request is for text
		SimpleDateFormat d = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
		String date = d.format(new Date());
		addToResponse("Date: " + date);
		addToResponse("Last-Modified: " + date);
		addToResponse("Content-Length: " + length);
	}
	//Robert Alianello
	public void setResponseHeaders() {
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
			String sessionID = checkSession(requestHeaders);
			if (HttpSession.isSet(sessionID)) {
				// check if sessionID is in sessions collection. Login user if true
				// -- grab MailServerConnection thread from sessions collection and assign to this thread
				this.mainConnection = (MailServerConnection)HttpSession.get(sessionID);
				this.user = mainConnection.getUser();
				//update the time logged for this session ID so it stays active
				HttpSession.updateTime(sessionID);
				this.validSession = true;
			}
			//disallow all requests with no valid session, except those attempting to authenticate
			else if (!(path.equals("/signin") && this.method == RequestType.POST)) {
				//If no valid session, show HTTP 403 (access denied)and return
				out.write(Main.serveLogin().getBytes());
				this.closeAll();
				return;
			}
			
			
			String responseBody = ""; //body that will be sent to client
			
			String acceptField = requestHeaders.get("Accept");
			//handle different request methods
			if (method == RequestType.GET) {
				//handle GET requests for text
				if (acceptField.indexOf("text") >= 0) {
					if (acceptField.indexOf("html") >= 0) {
						responseBody = handleGetRequest(path);
						setResponseHeaders(new Integer(responseBody.length()).toString());
						addToResponse("Content-Type: text/html");
					}
					else if (acceptField.indexOf("css") >= 0) {
						responseBody = this.handleScript(path);
						setResponseHeaders(new Integer(responseBody.length()).toString());
						addToResponse("Content-Type: text/css");
					}

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
					for (int j = 0; j < filesize; i++, j++) responseBytes[i] = fileBytes[j];
					out.write(responseBytes);
					isText = false;
				}
				else if (acceptField.indexOf("*/*") >= 0) {
					if (path.length() > 3) {
						String ext = path.substring(path.length() - 3);
						if (ext.equals(".js")) {
							responseBody = this.handleScript(path);
							setResponseHeaders(new Integer(responseBody.length()).toString());
							addToResponse("Content-Type: text/javascript; charset=UTF-8");
						}
					}
				}
			}
			else if (method == RequestType.POST) {

				responseBody = handlePostRequest(path);
				setResponseHeaders(new Integer(responseBody.length()).toString());
				addToResponse("Content-Type: text/html");
			}
			else setResponseHeaders();
			
			//output response if the request was for text. Combine response headers with response content and send the bytes to the browser
			if (this.isText) out.write((responseHeaders + "\r\n" + responseBody).getBytes());

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
	public void startSession(String username, Socket s, DHEncryptionIO io) {
		String sessionID = HttpSession.start(username, s, io);
		addToResponse("Set-Cookie: SECMAILSESSIONID=" + sessionID);
		
	}
	//Robert Alianello
	public void addToResponse(String s) {
		//add field to response header
		this.responseHeaders = this.responseHeaders + s + "\r\n";
	}
	//Robert Alianello
	private String handleGetRequest(String path) {
		if (path.equals("/")) return setResponseBody(new Home(this.mainConnection));
		else if (path.equals("/whoami")) return setResponseBody(new LoggedInAs(this.mainConnection));
		else if (path.equals("/signout")) return setResponseBody(new SignOut(this.mainConnection));
		else if (path.equals("/inbox")) return setResponseBody(new Inbox(this.mainConnection));
		else if (path.equals("/newemail")) return setResponseBody(new SendEmail(this.mainConnection));
		else if (path.contains(".js") || path.contains(".css")) return handleScript(path);
		else return Main.serveBadRequest();
	}
	//Robert Alianello
	private String handlePostRequest(String path) {
		if (path.equals("/signin")) return setResponseBody(new SignIn(this.mainConnection, this));
		if (path.equals("/readmail")) return setResponseBody(new ReadEmail(this.mainConnection, this));
		if (path.equals("/sendmail")) return setResponseBody(new ProcessEmail(this.mainConnection, this));
		else return Main.serveBadRequest();
	}
	//Robert Alianello
	private byte[] handleFileRequest(String path) {
		byte[] bytes;
		try {
			File f = new File(path.substring(1));
			FileInputStream reader = new FileInputStream(f);
			bytes = new byte[(int)f.length()];
			reader.read(bytes);
			reader.close();
			return bytes;
		} 
		catch (IOException e) {System.out.println("bad file");}//TO DO -- do something better here
		return null;
		
	}
	//Robert Alianello
	private String handleScript(String path) {
		StringBuilder script = new StringBuilder();
		try {
			File f = new File(path.substring(1));
			BufferedReader r = new BufferedReader(new FileReader(f));
			String s;
			while ((s = r.readLine()) != null) {
				script.append(s);
			}
			r.close();
		}
		catch (IOException e) {}
		return script.toString();
	}
	//Robert Alianello
	public boolean handleLogin(String username, String password) {
		try {
			Socket s = new Socket(Main.host, Main.port);
			DHEncryptionIO io = new DHEncryptionIO(s, false);
			io.writeObject(new PacketHeader(Command.LOGIN));
			io.writeObject(username);
			io.writeObject(password);
			PacketHeader resp = (PacketHeader)io.readObject();
			if (resp.getCommand() == Command.LOGIN_SUCCESS) {
				//generate sessionID and store session. Add cookie to response
				startSession(username, s, io);
				return true;
			}
			else {
				io.writeObject(new PacketHeader(Command.CLOSE));
				io.close();
				s.close();
				io = null;
				return false;
			}
		}
		catch (IOException e) { return false; }
		catch (ClassNotFoundException e) { return false; }

	}
	//Robert Alianello
	private void addGetParams(HashMap<String, String> rHeaders, String[] loc) {
		String[] params = loc[1].split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) rHeaders.put(p[0], p[1]);
		}
	}
	//Robert Alianello
	private String setResponseBody(Content c) {
		//returns a string representing the Content object c
		ArrayList<String> headers;
		if ((headers = c.getAddedReponseHeaders()) != null) {
			for (String s : headers) addToResponse(s);
		}
		return c.display();
	}
	
	public HashMap<String, String> getRequestHeaders() {
		return this.requestHeaders;
	}
	//Robert Alianello
	private void closeAll() {
		try {
			this.clientSock.close();
			this.in.close();
			this.out.close();
			this.mainConnection = null;
		} catch (IOException e) { System.out.println("error while closing connection"); }
	}
	

}
