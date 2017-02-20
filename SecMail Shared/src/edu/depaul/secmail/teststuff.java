package edu.depaul.secmail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class teststuff {
	
	public String pass;
	private static File file = new File("users.bin");
	

	
	 

	public static void main(String[] args) {
		//teststuff t = new teststuff();
		Auth.createAccount("test3", "test3");
	}

}
