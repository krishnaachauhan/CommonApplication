package com.easynet.util;

import java.security.Key;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
public class GenerateToken {

	private static final String ALGO = "AES/CBC/PKCS5Padding";
	private static final byte[] keyValue = "GeNe_@ut#_t0!<en".getBytes();
	private static Key key;
	private static Cipher decryptor;
	private static Cipher encryptor;

	private static Key generateKey() throws Exception{
		if (key == null)
			key = new SecretKeySpec(keyValue, "AES");
		return key;
	}

	public static void init() throws Exception {
		key = generateKey();
		encryptor = Cipher.getInstance(ALGO);
		IvParameterSpec iv = new IvParameterSpec(Hex.decodeHex("12345678901234567890123456789012".toCharArray()));
		decryptor = Cipher.getInstance(ALGO);
		encryptor.init(Cipher.ENCRYPT_MODE, key, iv);
		decryptor.init(Cipher.DECRYPT_MODE, key, iv);
	}

	public static String encrypt(String Data) throws Exception {
		byte[] encVal = encryptor.doFinal(Data.getBytes());
		String encryptedValue = Base64.getEncoder().encodeToString(encVal);				
		return encryptedValue;
	}

	public static String decrypt(String encryptedData) throws Exception {
		byte[] decordedValue =Base64.getDecoder().decode(encryptedData); 			
		byte[] decValue = decryptor.doFinal(decordedValue);
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private static String getMD5_2(String input_data) {
		byte[] source = input_data.getBytes();
		String s = null;
		try {			
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte[] tmp = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : tmp) {
				sb.append(String.format("%02X", b));
			}
			s = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static String getToken() throws Exception {
		init();
		byte[] maintext = null;				
		String password = String.valueOf(System.currentTimeMillis());
		String passwordEnc = encrypt(password);
		String md5 = getMD5_2(passwordEnc);
		
		maintext =Base64.getEncoder().encode((md5 +"."+ password).getBytes());		
		return new String(maintext);	
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(getToken());
	}
}
