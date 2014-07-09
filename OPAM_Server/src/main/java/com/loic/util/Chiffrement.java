package com.loic.util;

import javax.crypto.Cipher;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Chiffrement {
	//chiffrement les mots de passe
	public static String encrypt(String content, String cle) {  
        try {   
                SecretKeySpec key = new SecretKeySpec(getkeybyte(cle), "AES");  
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// create le crypteur
                IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
                byte[] byteContent = content.getBytes("utf-8");  
                cipher.init(Cipher.ENCRYPT_MODE, key,iv);// initiation
                byte[] result = cipher.doFinal(byteContent);                
                return Byte2Hex(result);    
        } catch (Exception e) {  
                e.printStackTrace();  
                return null; 
        } 
         
	}
	
	//d�chiffrement les mots de passe
	public static String decrypt(String content, String cle) {  
        try {  
        	SecretKeySpec key = new SecretKeySpec(getkeybyte(cle), "AES");              
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] result = cipher.doFinal(Hex2Byte(content));  
            return new String(result);
        } catch (Exception e) {  
                e.printStackTrace();  
                return null; 
        } 
	} 
	
	private static byte[] getkeybyte(String key)throws Exception{
		byte[] orignie = key.getBytes();
		byte[] keyByte = new byte[16];
		for(int i=0;i<16;i++){
			keyByte[i]=orignie[i%orignie.length];
		}
		return keyByte;
	}
	
	//byte to Hex
	public static String Byte2Hex(byte buf[]) throws Exception{  
        StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < buf.length; i++) {  
                String hex = Integer.toHexString(buf[i] & 0xFF);  
                if (hex.length() == 1) {  
                        hex = '0' + hex;  
                }  
                sb.append(hex.toUpperCase());  
        }  
        return sb.toString();  
	}
	
	//Hex to Byte
	public static byte[] Hex2Byte(String hexStr) throws Exception{  
        if (hexStr.length() < 1)  
                return null;  
        byte[] result = new byte[hexStr.length()/2];  
        for (int i = 0;i< hexStr.length()/2; i++) {  
                int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
                int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
                result[i] = (byte) (high * 16 + low);  
        }  
        return result;  
	} 
}