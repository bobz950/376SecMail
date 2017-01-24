/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

//debug connection: 127.0.0.1:57890

public class SecMailStaticEncryption {
	
	private static String filePath;

	private static byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; //AES initialization vector, should not remain 0s
    private static IvParameterSpec ivspec = new IvParameterSpec(iv);
    private static final String ENCRYPTIONSPEC = "AES/CBC/PKCS5Padding"; //
    
    //MARK: - Text Encryption
    public static void encryptText(String text, byte[] key) throws IOException { //Clayton Cohn
    	byte[] encryptedText = SecMailEncryptAES(text, key);
    	File tempFile = File.createTempFile("smTEXT", ".tmp", null);
    	FileOutputStream fileOut = new FileOutputStream(tempFile);
    	fileOut.write(encryptedText);
    	fileOut.close();
    	filePath = tempFile.getAbsolutePath();
//    	System.out.println("Text to encrypt: " + text);
//    	System.out.println("Text encrypted: " + new String (encryptedText, StandardCharsets.UTF_8));
//    	System.out.println("File path of encrypted text: " + filePath);
    }
    
//    public static String readFile(String filename) throws IOException{
//        String content = null;
//        File file = new File(filename); 
//        FileReader reader = null;
//        try {
//            reader = new FileReader(file);
//            char[] chars = new char[(int) file.length()];
//            reader.read(chars);
//            content = new String(chars);
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if(reader !=null){reader.close();}
//        }
//        return content;
//    }
    
    public static String decryptText(String filePathString, byte[] key) throws IOException { //Clayton Cohn
    	File file = new File(filePathString);
    	FileInputStream fileStream = new FileInputStream(file);
    	byte encryptedBytes[] = new byte[fileStream.available()];
    	fileStream.read(encryptedBytes);
    	String decryptedText = SecMailDecryptAES(encryptedBytes, key);
    	fileStream.close();
    	return decryptedText;
    }

//    //MARK: - File Encryption
//    public static void encryptFile(File file, byte[] key) throws IOException {
//    	SealedObject obj = encryptObject(file, key);
//    	File tempFile = File.createTempFile("smFILE", ".tmp", null);
//    	FileOutputStream fileOut = new FileOutputStream(tempFile);
//    	ObjectOutputStream opStream = new ObjectOutputStream(fileOut);
//    	opStream.writeObject(obj);
//    	opStream.close();
//    }
//    
//    public static File decryptFile(File file, byte[] key) throws IOException {
//    	//TODO
//    }
    
    //following java implementation here: http://www.java2s.com/Tutorial/Java/0490__Security/ImplementingtheDiffieHellmankeyexchange.htm
    //									  http://www.java2s.com/Tutorial/Java/0490__Security/DiffieHellmanKeyAgreement.htm
    
    public static class DHKeyServer implements Runnable{
    	private BigInteger Modulo; //also known as p
    	private BigInteger Base; //also known as generator, g
    	private int bitLen;
    	private Socket clientSocket;
    	private KeyAgreement serverKeyAgree;
    	private KeyPair serverPair;
    	private KeyPairGenerator kpg;
    	private byte key[];
    	
    	private InputStream clientIn;
    	private DataOutputStream clientDataOut;
    	private OutputStream clientOut;
    	
    	public byte[] getKey() {
			return key;
		}
    	
    	public void run(){
    		this.serverInit();
    		this.key = this.doExchange();
    	}
    	
    	public DHKeyServer(Socket client, int BitLen){
    	    this.clientSocket = client;
    	    this.bitLen = BitLen;
    	    this.key = null;
    	    
    	}
    	
    	
    	public void serverInit(){
    		SecureRandom rnd = new SecureRandom();
    	    this.Modulo = BigInteger.probablePrime(bitLen, rnd);
    	    this.Base = BigInteger.probablePrime(bitLen, rnd);
    	    //send these over the socket
    	    
    	    try {
    	    	
    	    	this.kpg = KeyPairGenerator.getInstance("DiffieHellman");
    	    	kpg.initialize(bitLen);
    	    	this.serverKeyAgree = KeyAgreement.getInstance("DH");
    	    	this.serverPair = kpg.generateKeyPair();
    	    	
    	    	
    	    	this.clientIn = clientSocket.getInputStream();
    	    	this.clientOut = clientSocket.getOutputStream();
    	    	
    	    	this.clientDataOut = new DataOutputStream(clientOut);
    	    	
    	    	    	    	
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
    		
	    }
    	
    	public byte[] doExchange(){
    		
    		try{
    			
    			//sends the server's generated key to the client
    			    			
    			byte serverPublicKey[] = serverPair.getPublic().getEncoded();    			
    			int len = serverPublicKey.length;
    			
    			clientDataOut.writeInt(len);
    			clientDataOut.flush();    			
    			clientDataOut.write(serverPublicKey);
    			clientDataOut.flush();
    			
    			//receives the key the client generated
    			
			    int clientPubKeyLen = clientIn.read();
			    byte clientPubKeyBytes[] = new byte[clientPubKeyLen];
			    clientIn.read(clientPubKeyBytes, 0, clientPubKeyLen);
			    
			    //decodes the client's key
			    
			    KeyFactory kf = KeyFactory.getInstance("DiffieHellman");
			    X509EncodedKeySpec ks = new X509EncodedKeySpec(clientPubKeyBytes);
			    PublicKey clientPubKey = kf.generatePublic(ks);
			    
			    
			    //does the Diffie-Hellman operations
			    serverKeyAgree.init(serverPair.getPrivate());
			    serverKeyAgree.doPhase(clientPubKey, true);
			    byte temp[] = serverKeyAgree.generateSecret();
			    
			    return temp;
			    
			    
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
    		
    		return null;
    		
    	}
    	
    	
    	
    }
    
    	
    //Clayton Newmiller
    public static class DHKeyClient implements Runnable{
    	private Socket serverSocket;
    	private InputStream serverIn;
    	//DataOutputStream readInt()
    	private DataInputStream serverDataIn;
    	private OutputStream serverOut;
    	private KeyPairGenerator kpg;
    	private byte key[];
    	
    	
    	
    	public void run(){
    		this.clientInit();
    		this.key = this.doExchange();
    	}
    	
    	public DHKeyClient(Socket s){
    	    this.serverSocket = s;
    	    
    	}
    	
    	public void clientInit(){
    	    try {
    	    	this.serverIn = serverSocket.getInputStream();
    	    	this.serverOut = serverSocket.getOutputStream();
				this.kpg = KeyPairGenerator.getInstance("DiffieHellman");
				this.key = null;
				
				
			    
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
    		
    		
	    }
    	public byte[] doExchange(){
    		try{
    			this.serverDataIn= new DataInputStream(serverIn);
    			//reads the server's key
				int serverPubKeyLen = serverDataIn.readInt();
				byte serverPubKeyBytes[] = new byte[serverPubKeyLen];
				this.serverIn.read(serverPubKeyBytes, 0, serverPubKeyLen);
				
				//decodes the server key and finds its parameters
				X509EncodedKeySpec ks = new X509EncodedKeySpec(serverPubKeyBytes);
				KeyFactory kf = KeyFactory.getInstance("DH");
				PublicKey serverPubKey = kf.generatePublic(ks);
				DHParameterSpec serverParameters = ((DHPublicKey)serverPubKey).getParams();
				kpg.initialize(serverParameters);
				
				//generates client key
				KeyAgreement clientKeyAgreement = KeyAgreement.getInstance("DH");
			    KeyPair clientPair = kpg.generateKeyPair();
			    clientKeyAgreement.init(clientPair.getPrivate());
			    
			    //sends client key back to server
			    byte clientPublicKey[] = clientPair.getPublic().getEncoded();
			    serverOut.write(clientPublicKey.length);
			    serverOut.flush();
			    serverOut.write(clientPublicKey);
			    serverOut.flush();
			    
			    //does the Diffie-Hellman operations
			    KeyAgreement clientKeyAgree = KeyAgreement.getInstance("DH");
			    clientKeyAgree.init(clientPair.getPrivate());
			    clientKeyAgree.doPhase(serverPubKey, true);
			    byte temp[] = clientKeyAgree.generateSecret();
			    
			    return temp;			    
			    
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
    		
    		return null;
    	}


    	public byte[] getKey() {
			return key;
		}


    		
    }
    
    
    
	//Clayton Newmiller
	//works on any length message or key, as long as it uses ENCRYPTIONSPEC
    public static byte[] SecMailEncryptAES(String message, byte keyBytes[]){
		
		byte cipherText[] = null;
		byte messageBytes[] = message.getBytes();
		
		
		try {
			SecretKeySpec keyspec = new SecretKeySpec(keyBytes, "AES");
			Cipher c = Cipher.getInstance(ENCRYPTIONSPEC);
			c.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			
			cipherText = c.doFinal(messageBytes);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return cipherText;
	}
	
	//Clayton Newmiller
    public static String SecMailDecryptAES(byte[] cipherText, byte keyBytes[]){
		
		String message = null;
		
		try {
			SecretKeySpec keyspec = new SecretKeySpec(keyBytes, "AES");
			Cipher c = Cipher.getInstance(ENCRYPTIONSPEC);
			c.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte cipherBytes[] = c.doFinal(cipherText);
			message = new String (cipherBytes, StandardCharsets.UTF_8);
			
			
		} catch (Exception e) {
			e.printStackTrace(); //this already prints, no need to call System.out.println
		}
		return message;
	}

	public static SealedObject encryptObject(Serializable object, byte keyBytes[]){
		SecretKeySpec keyspec = new SecretKeySpec(keyBytes, "AES");
		Cipher c=null;
		SealedObject encryptedPacket=null;
		try {
			c = Cipher.getInstance(ENCRYPTIONSPEC);
			c.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			encryptedPacket = new SealedObject(object, c);
			
			return encryptedPacket;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Serializable decryptObject(SealedObject object, byte keyBytes[]){ //needs to be cast to what it actually is
		SecretKeySpec keyspec = new SecretKeySpec(keyBytes, "AES");
		Cipher c=null;
		Serializable decryptedObject=null;
		try {
			c = Cipher.getInstance(ENCRYPTIONSPEC);
			c.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			decryptedObject = (Serializable) object.getObject(c); //problem here: improperly padded - WRONG KEYS
			return decryptedObject;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*public static void main(String[] args) throws IOException {	
		
		
		encryptText("this is the decrypted text", iv);
		System.out.println(decryptText(filePath, iv));
//		String s = "hello";
//		byte[] key = ConvertStringToByteArray("12345678");
//		SealedObject enc = encryptObject(s, key);
//		String b = (String) decryptObject(enc, key);
//		System.out.println(b);
		
		

	}*/

}
