package edu.depaul.secmail.content;


import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based two factor authentication algorithm. 
 *
 * Use generateBase32Secret() to generate a secret key for a user.
 * Store the secret key in the database associated with the user account.
 * Display the QR image URL returned by qrImageUrl(...) to the user.
 * User uses the image to load the secret key into the authenticator application.
 *
 * Whenever the user logs in:
 *
 *The user enters the number from the authenticator application into the login window.
 *Read the secret associated with the user account from the database.
 *The server compares the user input with the output from generateCurrentNumber(...).
 *If they are equal then the user is allowed to log in.
 *
 */
public class TwoFactorAuthentication {
	//Rohail Baig
    //30 second wait time to enter authentication
    public static final int DEFAULT_TIME_STEP_SECONDS = 30;
    //two-factor authentication code cannot exceed 6 digits.
    private static int NUM_DIGITS_OUTPUT = 6;
    
    private static final String blockOfZeros;
    
    static {
        char[] chars = new char[NUM_DIGITS_OUTPUT];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = '0';
        }
        blockOfZeros = new String(chars);
    }
    
    
//     Generate and return a secret key in base32 format (A-Z2-7). Could be used to generate
//     the QR image to be shared with the user.
     //Rohail Baig
    public static String generateBase32Secret() {
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            int val = random.nextInt(32);
            if (val < 26) {
                sb.append((char) ('A' + val));
            } else {
                sb.append((char) ('2' + (val - 26)));
            }
        }
        return sb.toString();
    }
    
    /**
     * Return the current number to be checked. This can be compared against user input.
     *
     * 
     * 
     * For more details of this algorithm, see:
     * http://en.wikipedia.org/wiki/Time-based_One-time_Password_Algorithm
     *
     *Secret string that was used to generate the QR code or shared with the user.
     */
    public static String generateCurrentNumber(String secret) throws GeneralSecurityException {
        return generateCurrentNumber(secret, System.currentTimeMillis(), DEFAULT_TIME_STEP_SECONDS);
    }
    
    //Rohail Baig
    public static String generateCurrentNumber(String secret, long currentTimeMillis, int timeStepSeconds)
    throws GeneralSecurityException {
        
        byte[] key = decodeBase32(secret);
        
        byte[] data = new byte[8];
        long value = currentTimeMillis / 1000 / timeStepSeconds;
        for (int i = 7; value > 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        
        // encrypt the data with the key and return the SHA1 of it in hex
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        
        // take the 4 least significant bits from the encrypted string as an offset
        int offset = hash[hash.length - 1] & 0xF;
        
        // Using a long because Java does not support unsigned bits. 
        long truncatedHash = 0;
        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            // get the 4 bytes at the offset
            truncatedHash |= (hash[i] & 0xFF);
        }
        // cut off the top bit
        truncatedHash &= 0x7FFFFFFF;
        
        // the token is then the last 6 digits in the number
        truncatedHash %= 1000000;
        
        return zeroPrepend(truncatedHash, NUM_DIGITS_OUTPUT);
    }
    
    /**
     * Return the QR image url using Google. This can be shown to the user and scanned by the authenticator program
     * as an easy way to enter the secret.
     *
     */
    
    //Rohail Baig
    public static String qrImageUrl(String keyId, String secret) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("https://chart.googleapis.com/chart");
        sb.append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=");
        sb.append("otpauth://totp/").append(keyId).append("%3Fsecret%3D").append(secret);
        return sb.toString();
    }
    
    
    // Return the string prepended with 0s.
    //Rohail Baig
    static String zeroPrepend(long num, int digits) {
        String numStr = Long.toString(num);
        if (numStr.length() >= digits) {
            return numStr;
        } else {
            StringBuilder sb = new StringBuilder(digits);
            int zeroCount = digits - numStr.length();
            sb.append(blockOfZeros, 0, zeroCount);
            sb.append(numStr);
            return sb.toString();
        }
    }
    
    //Decode 32-Base Method.
    //Rohail Baig
    static byte[] decodeBase32(String str) {
        // each base-32 character encodes 5 bits
        int numBytes = ((str.length() * 5) + 4) / 8;
        byte[] result = new byte[numBytes];
        int resultIndex = 0;
        int which = 0;
        int working = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int val;
            if (ch >= 'a' && ch <= 'z') {
                val = ch - 'a';
            } else if (ch >= 'A' && ch <= 'Z') {
                val = ch - 'A';
            } else if (ch >= '2' && ch <= '7') {
                val = 26 + (ch - '2');
            } else if (ch == '=') {
                // special case
                which = 0;
                break;
            } else {
                throw new IllegalArgumentException("Invalid base-32 character: " + ch);
            }
            
            switch (which) {
                case 0:
                    // all 5 bits is top 5 bits
                    working = (val & 0x1F) << 3;
                    which = 1;
                    break;
                case 1:
                    // top 3 bits is lower 3 bits
                    working |= (val & 0x1C) >> 2;
                    result[resultIndex++] = (byte) working;
                    // lower 2 bits is upper 2 bits
                    working = (val & 0x03) << 6;
                    which = 2;
                    break;
                case 2:
                    // all 5 bits is mid 5 bits
                    working |= (val & 0x1F) << 1;
                    which = 3;
                    break;
                case 3:
                    // top 1 bit is lowest 1 bit
                    working |= (val & 0x10) >> 4;
                    result[resultIndex++] = (byte) working;
                    // lower 4 bits is top 4 bits
                    working = (val & 0x0F) << 4;
                    which = 4;
                    break;
                case 4:
                    // top 4 bits is lowest 4 bits
                    working |= (val & 0x1E) >> 1;
                    result[resultIndex++] = (byte) working;
                    // lower 1 bit is top 1 bit
                    working = (val & 0x01) << 7;
                    which = 5;
                    break;
                case 5:
                    // all 5 bits is mid 5 bits
                    working |= (val & 0x1F) << 2;
                    which = 6;
                    break;
                case 6:
                    // top 2 bits is lowest 2 bits
                    working |= (val & 0x18) >> 3;
                    result[resultIndex++] = (byte) working;
                    // lower 3 bits of byte 6 is top 3 bits
                    working = (val & 0x07) << 5;
                    which = 7;
                    break;
                case 7:
                    // all 5 bits is lower 5 bits
                    working |= (val & 0x1F);
                    result[resultIndex++] = (byte) working;
                    which = 0;
                    break;
            }
        }
        if (which != 0) {
            result[resultIndex++] = (byte) working;
        }
        if (resultIndex != result.length) {
            result = Arrays.copyOf(result, resultIndex);
        }
        return result;
    }
    //Rohail Baig
    //Testing the encoded outome of the return value that transmit to Authenticator application. 
    public static void main (String [] Args) {
    //	TwoFactorAuthentication  zero = zeroPrepend(num, digits);
    	
    	byte[] result = decodeBase32("helkop");
    	System.out.println(result);
    	
    }
}
