package com.app.damnvulnerablebank;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final String MODE = "RSA/ECB/PKCS1Padding";

    private static final String key = "bf3c199c2470cb477d907b1e0917c17b"; //　JO1QhyBz6ywDQ8sm5vWJCk3z55vFmQ9DWQaXRI1P2s56jsKpILo1CM7y5/74+TwQ/m12sIUjbpfOQAhjHjcWoYxitR7Djq5jyHgFJS6VuGiT2elhqmQnQOhLEmPAr1R4Y+nJHyjCjdLDAwqKFtA7WJiXBJCOirrKra69fO+4R4C9MeQW6P2IQ5wBDRfwcl8VkKnlZyQzbSUcKQSUAUPYZUD92f+5YbTRgGsiSH8rnx7p1PJXkeMeWUqsA9GQC8K5UrWh1tMlsblH2oG+xqvWrDaZUb4E1xrNJf8sGOJM+jsGlFXOxgZLDSa2dvqwUDdmZDhNd1T7hGo4Ttk4U/gbXg==
    private static final String iv = "5183666c72eec9e4"; //　BZZz4cueSgUnd3cpLgbxmQ+3BEzQiaE9SkklQBw1ZNesWaKNLYxInuC4drzSsG3QktZdyva5PnzwwYItG//E7xUwMVPsoYEDBW1O9/ytd5lOgFp6KoXzieCq9tlgIatck0cBZAZ8Mc+qBmtDRVoCpQ4YVAeqDVCHPcwhYoLRqSyy2//tZ/iCeOQFmCCM5/Ka6ni/dcJ6WUs6y/Gq5avPwaeInhno3ne6JhygO2C5lTwweEVe/oKt5KM/IOLsbvfDIxeevZHuSs9q+VIgM+kKQr9XdCJr9CBXaFweYobg6U+Z+vQTtQcgGiH3TF7i8PKCuBKzwnixn+KpxleJmTd6aQ==
    public static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String ALIAS = "cipher";

    public static KeyStore keyStore;

    private static SharedPreferences pref;
    private Context context;

    public EncryptDecrypt(Context context) {
        this.context = context;
        pref = context.getSharedPreferences("apiurl", MODE_PRIVATE);
    }

    public static void init() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(ALIAS)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE);
                KeyGenParameterSpec keyGenParameterSpec = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                            ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .build();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyPairGenerator.initialize(keyGenParameterSpec);
                }
                keyPairGenerator.generateKeyPair();
            }
        } catch (KeyStoreException e) {
            Log.e("EncryptDecrypt.init", "KeyStoreException");
        }catch (CertificateException e) {
            Log.e("EncryptDecrypt.init", "CertificateException");
        }catch (IOException e) {
            Log.e("EncryptDecrypt.init", "IOException");
        } catch (NoSuchAlgorithmException e){
            Log.e("EncryptDecrypt.init", "NoSuchAlgorithmException");
        } catch (InvalidAlgorithmParameterException e){
            Log.e("EncryptDecrypt.init", "InvalidAlgorithmParameterException");
        } catch (Exception e) {
            Log.e("EncryptDecrypt.init", "Exception");
        }
    }

    public static String encrypt(String data) {
        init();
        Log.d("EncryptDecrypt TEST2", encryptByANDROID_KEY_STORE(key));
        Log.d("EncryptDecrypt TEST3", encryptByANDROID_KEY_STORE(iv));
        return encryptByAES256(data, key, iv);
    }

    public static String decrypt(String data) {
        return decryptByAES256(data, key, iv);
    }

    public static String getApiUri() {
        String apiUri = "";
        String encryptedApiUri = pref.getString("apiurl", null);
        if (encryptedApiUri != null) {
            return decrypt(encryptedApiUri);
        }
        return apiUri;
    }

    public static String encryptByAES256(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            // 암호화 알고리즘이 지원되지 않을 때의 예외 처리
            Log.e("EncryptDecrypt", "NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e) {
            // 지정된 패딩이 지원되지 않을 때의 예외 처리
            Log.e("EncryptDecrypt", "NoSuchPaddingException");
        } catch (InvalidKeyException e) {
            // 잘못된 키로 인해 암호화 초기화에 실패했을 때의 예외 처리
            Log.e("EncryptDecrypt", "InvalidKeyException");
        } catch (InvalidAlgorithmParameterException e) {
            // 잘못된 암호화 알고리즘 파라미터로 초기화에 실패했을 때의 예외 처리
            Log.e("EncryptDecrypt", "InvalidAlgorithmParameterException");
        } catch (IllegalBlockSizeException e) {
            // 잘못된 블록 크기로 암호화 시도했을 때의 예외 처리
            Log.e("EncryptDecrypt", "IllegalBlockSizeException");
        } catch (BadPaddingException e) {
            // 잘못된 패딩으로 암호화 시도했을 때의 예외 처리
            Log.e("EncryptDecrypt", "BadPaddingException");
        } catch (Exception e) {
            // 기타 다른 예외들을 처리할 때의 예외 처리
            Log.e("EncryptDecrypt", "Exception");
        }
        return null;
    }

    public static String decryptByAES256(String encryptedData, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            // 암호화 알고리즘이 지원되지 않을 때의 예외 처리
            Log.e("EncryptDecrypt", "NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e) {
            // 지정된 패딩이 지원되지 않을 때의 예외 처리
            Log.e("EncryptDecrypt", "NoSuchPaddingException");
        } catch (InvalidKeyException e) {
            // 잘못된 키로 인해 암호화 초기화에 실패했을 때의 예외 처리
            Log.e("EncryptDecrypt", "InvalidKeyException");
        } catch (InvalidAlgorithmParameterException e) {
            // 잘못된 암호화 알고리즘 파라미터로 초기화에 실패했을 때의 예외 처리
            Log.e("EncryptDecrypt", "InvalidAlgorithmParameterException");
        } catch (IllegalBlockSizeException e) {
            // 잘못된 블록 크기로 암호화 시도했을 때의 예외 처리
            Log.e("EncryptDecrypt", "IllegalBlockSizeException");
        } catch (BadPaddingException e) {
            // 잘못된 패딩으로 암호화 시도했을 때의 예외 처리
            Log.e("EncryptDecrypt", "BadPaddingException");
        } catch (Exception e) {
            // 기타 다른 예외들을 처리할 때의 예외 처리
            Log.e("EncryptDecrypt", "Exception");
        }
        return null;
    }

    public static String encryptByANDROID_KEY_STORE(String data) {
        String encryptedData = "";
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(ALIAS).getPublicKey();
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

        } catch (KeyStoreException e) {
            Log.e("ANDROID_KEY_STORE", "KeyStoreException");
        }catch (CertificateException e) {
            Log.e("ANDROID_KEY_STORE", "CertificateException");
        }catch (IOException e) {
            Log.e("ANDROID_KEY_STORE", "IOException");
        } catch (NoSuchAlgorithmException e){
            Log.e("ANDROID_KEY_STORE", "NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e){
            Log.e("ANDROID_KEY_STORE", "NoSuchPaddingException");
        } catch (InvalidKeyException e){
            Log.e("ANDROID_KEY_STORE", "InvalidKeyException");
        } catch (IllegalBlockSizeException e){
            Log.e("ANDROID_KEY_STORE", "IllegalBlockSizeException");
        } catch (BadPaddingException e){
            Log.e("ANDROID_KEY_STORE", "BadPaddingException");
        } catch (Exception e) {
            // 기타 다른 예외들을 처리할 때의 예외 처리
            Log.e("EncryptDecrypt", e.toString());
        }
        return encryptedData;
    }

    public static String decryptByANDROID_KEY_STORE(String encryptedData) {
        String decryptedData = "";
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(ALIAS, null);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
            decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (KeyStoreException e) {
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "KeyStoreException");
        }catch (CertificateException e) {
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "CertificateException");
        }catch (IOException e) {
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "IOException");
        } catch (NoSuchAlgorithmException e){
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e){
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "NoSuchPaddingException");
        } catch (InvalidKeyException e){
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "InvalidKeyException");
        } catch (IllegalBlockSizeException e){
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "IllegalBlockSizeException");
        } catch (BadPaddingException e){
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "BadPaddingException");
        } catch (Exception e) {
            Log.e("EncryptDecrypt.decryptByANDROID_KEY_STORE", "Exception");
        }
        return decryptedData;
    }
}
