package edu.depaul.secmail;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {
	
	public static String host;
	public static int port;
	
	//Robert Alianello
	public static class HttpServer implements Runnable {
		private ServerSocket sSocket;
		//Robert Alianello
		public HttpServer() {
			try {
				this.sSocket = new ServerSocket(82);
			} 
			catch (IOException e) {
				System.out.println("IOException");
			}
		}
		//Robert Alianello
		public void run() {
			while (true) {
				try {
					Socket clientSock = sSocket.accept();
					Runnable con = new Thread(new HttpHandler(clientSock));
					((Thread)con).start();
				} 
				catch (IOException e) {
					System.out.println("IOException....");
				}
			}
		}
	}

	public static void main(String[] args) {
		//**Robert Alianello
		if (args.length > 1) {
			Main.host = args[0];
			Main.port = Integer.parseInt(args[1]);
		}
		else {
			//use default host/port if no command line args were used
			Main.host = "localhost";
			Main.port = 57890;
		}
		
		//Start thread listening for http connections
		System.out.println("SecMailHttpd Starting...");
		Runnable httpServ = new Thread(new HttpServer());
		((Thread)httpServ).start();
		
		//start session management thread
		Runnable sessionMan = new Thread(new HttpSession.sessionCleaner());
		((Thread)sessionMan).start();

	}
	
	//Robert Alianello
	protected synchronized static boolean handleLogin(String username, String password) {
		try {
			Socket s = new Socket(host, port);
			DHEncryptionIO io = new DHEncryptionIO(s, false);
			io.writeObject(new PacketHeader(Command.LOGIN));
			io.writeObject(username);
			io.writeObject(password);
			PacketHeader resp = (PacketHeader)io.readObject();
			if (resp.getCommand() == Command.LOGIN_SUCCESS) {
				//generate sessionID and store session. Add cookie to response
				//startSession(username);
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
	
	public static String serve401() {
		String header = "HTTP/1.1 200 OK\r\n"
				+ "Server: SecMail HTTP Server 1.0\r\n"
				+ "Content-Type: text/html\r\n"
				+ "Connection: Close\r\n\r\n";
		return header + "<h1>Access Denied!</h1>";
	}
	
	public static String serveBadRequest() {
		//TODO
		return "";
	}
	
	public static String serveLogin() {
		String header = "HTTP/1.1 200 OK\r\n"
				+ "Server: SecMail HTTP Server 1.0\r\n"
				+ "Content-Type: text/html\r\n"
				+ "Connection: Close\r\n";
		String c = "<form method='post' action='/signin'><input id='name' name='name' type='text'><input id='pass' name='pass' type='text'><button>Login</button</form>";
		header = header.concat("Content-Length: " + c.length() + "\r\n\r\n");
		return header.concat(c);
	}

}
