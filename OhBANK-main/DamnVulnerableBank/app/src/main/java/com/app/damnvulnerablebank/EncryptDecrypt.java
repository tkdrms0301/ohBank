package com.app.damnvulnerablebank;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static final String key = "bf3c199c2470cb477d907b1e0917c17b";
    private static final String iv = "5183666c72eec9e4";

    public static String encrypt(String data) {
        return encryptByAES256(data, key, iv);
    }

    public static String decrypt(String data) {
        return decryptByAES256(data, key, iv);
    }

    public static String encryptByAES256(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptByAES256(String encryptedData, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    static public String secret = "amazing";
//    static public int secretLength = secret.length();
//
//    public static String operate(String input) {
//        String result = "";
//        for(int i = 0; i < input.length(); i++) {
//            int xorVal = (int) input.charAt(i) ^ (int) secret.charAt(i % secretLength);
//            char xorChar =  (char) xorVal;
//
//            result += xorChar;
//        }
//
//        return result;
//    }
//
//    public static String encrypt(String input) {
//        String encVal = operate(input);
//        String val = Base64.encodeToString(encVal.getBytes(),0);
//
//        return val;
//    }
//
//    public static String decrypt(String input) {
//        byte[] decodeByte = Base64.decode(input,0);
//        String decodeString = new String(decodeByte);
//        String decryptString = operate(decodeString);
//
//        return decryptString;
//    }
}
