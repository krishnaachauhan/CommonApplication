/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.easynet.util;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

/**
 * @date 09-APR-2018
 * @author hp
 */
//@WebServlet(name = "AESEncryption", urlPatterns = {"/AESEncryption"})
public class AESEncryption {

    /**
     * gets the AES encryption key. In your actual programs, this should be
     * safely stored.
     *
     * @return
     * @throws Exception
     */
    public static SecretKey getSecretEncryptionKey() throws Exception {

        String mykey = "ACUTEAMCBMOBIPRO";
        //Generate the secret key specs.
        SecretKeySpec secretKey = new SecretKeySpec(mykey.getBytes("UTF8"), "AES");
        return secretKey;
    }

    /**
     * Encrypts plainText in AES using the secret key
     *
     * @param plainText
     * @return String
     * @throws Exception
     */
    public static String encryptText(String plainText) throws Exception {
        SecretKey secKey = AESEncryption.getSecretEncryptionKey();
        //AES defaults to AES in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes("UTF-8"));
        return AESEncryption.bytesToHex(byteCipherText);
    }

    /**
     * Decrypts encrypted byte array using the key used for encryption.
     *
     * @param data
     * @return String
     * @throws Exception
     */
    public static String decryptText(String data) throws Exception {
        SecretKey secKey = AESEncryption.getSecretEncryptionKey();
        byte[] byteCipherText = hexStringToByteArray(data);
        //AES defaults to AES in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secKey);
        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
        return new String(bytePlainText);
    }

    /**
     * Convert a binary byte array into readable hex form
     *
     * @param bytes 
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//    public static String bytesToHex(byte[] hash) {
//        
//        
//        return DatatypeConverter.printHexBinary(hash);
//    }
    /**
     * Convert a String to binary byte array into readable hex form
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String dataDecrypt(final String encrypted) {

        //SECRET_PASS_NETBANKING/
        try {

            //Base64.decode(encrypted);
            //SecretKey key = new SecretKeySpec(Base64.decode("u/Gu5posvwDsXUnV5Zaq4g=="), "AES");
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode("u/Gu5posvwDsXUnV5Zaq4g=="), "AES");
            //AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decode("5D9r9ZVzEYYgha93/aUK2w=="));
            AlgorithmParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode("5D9r9ZVzEYYgha93/aUK2w=="));
            //SecretKey key = new SecretKeySpec(Base64.decode("u/Gu5posvwDsXUnV5Zaq4g=="), "AES");
            //AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decode("5D9r9ZVzEYYgha93/aUK2w=="));
            byte[] decodeBase64 = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            String afterDecrypt = new String(cipher.doFinal(decodeBase64), "UTF-8");
            byte[] bytes = Hex.decodeHex(afterDecrypt.toCharArray());
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            //e.printStackTrace();
//            System.out.println("dataDecrypt : " + e.getMessage());
            //throw new RuntimeException("This should not happen in production.", e);
        }
        return "";
    }
//    
//    public static void main(String argu[]) throws Exception{
//        String data = "8EF890782EF831D226040584BBC4FB2B";
//        System.out.println(decryptText(data));
//    }
//    
//    public static SecretKey getSecretEncryptionKey(String sender_id, String mobile_no) throws Exception {
//
//        String mykey = sender_id + "|" + mobile_no;
//        mykey = mykey.substring(0, 16);
//        //Generate the secret key specs.
//        SecretKeySpec secretKey = new SecretKeySpec(mykey.getBytes("UTF8"), "AES");
//        return secretKey;
//    }
//    public static String decryptText(String data, String sender_id, String mobile_no) throws Exception {
//        SecretKey secKey = AESEncryption.getSecretEncryptionKey(sender_id, mobile_no);
//        byte[] byteCipherText = hexStringToByteArray(data);
//        //AES defaults to AES in Java 7
//        Cipher aesCipher = Cipher.getInstance("AES");
//        aesCipher.init(Cipher.DECRYPT_MODE, secKey);
//        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);
//        return new String(bytePlainText);
//    }
}
