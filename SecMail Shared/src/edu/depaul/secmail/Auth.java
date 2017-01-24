/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.depaul.secmail;

/**
 *
 * @author DJ, Juan Sierra
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;


public class Auth {

    /**
     * @param args the command line arguments
     */

    private static final int space = 32;
    private static final int newLine = 10;
    private static File file = new File("users.bin");
    private static FileInputStream fromFile;

/********************** DOUG ***********************************************/

     private static String getHashedPassword(String passwordToHash, byte[] salt)
    {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }


    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

/******************* Juan Sierra ****************************************/

    //Create an account if user does not have an account
    public static void createAccount(String username, String password)
    {
      try{

        //create salt for user
        byte[] salt = getSalt();

        if (!file.exists())
        {
          file.createNewFile();
        }

        //create hashed pw for user
        String securePW = getHashedPassword(password, salt);

        //Store username, hased password, and salt inside a file
        FileOutputStream toFile = new FileOutputStream(file, true);
        toFile.write((username + " ").getBytes());
        toFile.write((securePW + " ").getBytes());
        toFile.write(salt);
        toFile.write(("\n").getBytes());
        toFile.close();
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

    //Login if user has account
    public static boolean login(String username, String password) {
      try {
        String storedPw = "";
        int readPw;
        int readBytes;
        byte[] bFile = new byte[1];   //store one byte at a time
        byte[] storedSalt;

        fromFile = new FileInputStream(file);

        //If usernames match check password
        if (checkName(username))
        {
          //Store password in storedPw until space (32)
          while ((readPw = fromFile.read()) != space)
            storedPw += (char) readPw;

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

            //Store salt insde byteArray until EOF or new line
            while ((readBytes = fromFile.read(bFile)) != -1 && bFile[0] != newLine)
                byteArray.write(bFile);

            byteArray.flush();

            storedSalt = byteArray.toByteArray();
            String hashedPw = getHashedPassword(password, storedSalt);

            //Compare passwords
            if (storedPw.equals(hashedPw))
              return true;
            else
              return false;
        }
        else
          return false;
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
      return false;
    }

    //Check if username given matches username on file
    private static boolean checkName(String username)
    {
      String storedName = "";
      boolean toReturn = false;
      int readName;
      long junk;

      try
      {
        //Store username in storedName until it reaches space (32)
        while ((readName = fromFile.read()) != space)
        {
          //If EOL return false (could not find name)
          if (readName == -1)
            return false;

          storedName += (char)readName;
        }

        //If usernames match return true
        if (username.equals(storedName))
          toReturn = true;
        else
        {
          //Read until it gets to next line (10)
          while ((junk = fromFile.read()) != newLine)
        	  junk = 0;

          //Repeat steps for new line
          toReturn = checkName(username);
        }
    }
    catch (Exception e)
    {
        System.out.println(e.getMessage());
    }
    return toReturn;
  }
}
