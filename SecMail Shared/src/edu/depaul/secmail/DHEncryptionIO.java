/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SealedObject;

import edu.depaul.secmail.SecMailStaticEncryption.DHKeyClient;
import edu.depaul.secmail.SecMailStaticEncryption.DHKeyServer;

import java.io.IOException;

public class DHEncryptionIO{
	private Socket s; //clientSocket
	private ObjectInputStream is;
	private ObjectOutputStream os;
	private byte key[];
	private MessageDigest hash;
	
	DHEncryptionIO(Socket socket, boolean isServer) throws IOException
	{
		if (socket==null){
			throw new NullPointerException("INSANITY");
		}
		
		this.s=socket;
		os = new ObjectOutputStream(s.getOutputStream());
		is = new ObjectInputStream(s.getInputStream());
		
		try {
			this.hash = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} //it's java's fault that this is not SHA-256; Java doesn't implement AES with 256 bit keys 
		
		if (isServer){
			DHKeyServer server = new DHKeyServer(s, 512);
			server.run();
			this.key= hash.digest(server.getKey());
		}
		else{
			DHKeyClient client = new DHKeyClient(s);
			client.run();
			this.key= hash.digest(client.getKey());
		}
				
	}
	
	public Serializable readObject() throws ClassNotFoundException, IOException{
		return SecMailStaticEncryption.decryptObject(((SealedObject)is.readObject()), this.key);
	}
	
	public void writeObject(Serializable obj) throws IOException{
		
		this.os.writeObject(SecMailStaticEncryption.encryptObject(obj, this.key));
	}
	
	
	public int read() throws IOException
	{
		return is.read();
	}
	
	public int read(byte[] b) throws IOException
	{
		return is.read(b);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return is.read(b, off, len);
	}
	
	public long skip(long n) throws IOException
	{
		return is.skip(n);
	}
	
	
	public int available() throws IOException
	{
		return is.available();
	}
	
	public void close() throws IOException
	{
		is.close();
		os.close();
	}
	
	
	public void mark(int readlimit)
	{
		is.mark(readlimit);
	}
	
	public void reset() throws IOException
	{
		is.reset();
	}
	
	public boolean markSupported()
	{
		return is.markSupported();
	}
	
	public void flush() throws IOException
	{
		os.flush();
	}
	
	
	
	public void write(byte[] b) throws IOException
	{
		os.write(b);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		os.write(b, off, len);
	}
	
	public void write(int b) throws IOException
	{
		os.write(b);
	}
	
}
