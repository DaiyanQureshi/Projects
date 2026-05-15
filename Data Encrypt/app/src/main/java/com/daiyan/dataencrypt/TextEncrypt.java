package com.daiyan.dataencrypt;

import java.util.ArrayList;
import java.util.List;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class TextEncrypt {
    private String plainText;
    private String password;


    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16; // Salt ke liye 16 bytes kaafi hain
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH_BIT = 256;


    public TextEncrypt(String Passtext, String Passpassword) {
        this.plainText = Passtext;
        this.password = Passpassword;
    }

    /**
     * 🔒 Encryption with AES-256 + SHA-256 Tag
     */
    public String encrypt() throws Exception {
        String plainText = this.plainText, password  = this.password;
        // 1. Password ka SHA-256 Hash nikalna (Verification ke liye)
        String hashTag = getSHA256Hash(password);

        // 2. Original Text ke aakhir mein Hash jod dena
        String dataWithTag = plainText + hashTag;

        // 3. Random Salt generate karna
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        new SecureRandom().nextBytes(salt);

        // 4. PBKDF2 se AES Key derive karna
        byte[] keyBytes = deriveKey(password, salt);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 5. Random IV generate karna
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);

        // 6. Cipher Initialize
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

        // 7. Data (with Tag) ko Encrypt karna
        byte[] cipherText = cipher.doFinal(dataWithTag.getBytes(StandardCharsets.UTF_8));

        // 8. [SALT + IV + CIPHERTEXT] teeno ko jodna
        byte[] combined = new byte[SALT_LENGTH_BYTE + IV_LENGTH_BYTE + cipherText.length];
        System.arraycopy(salt, 0, combined, 0, SALT_LENGTH_BYTE);
        System.arraycopy(iv, 0, combined, SALT_LENGTH_BYTE, IV_LENGTH_BYTE);
        System.arraycopy(cipherText, 0, combined, SALT_LENGTH_BYTE + IV_LENGTH_BYTE, cipherText.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    /**
     * 🔓 Decryption with AES-256 + SHA-256 Tag Check
     */
    public String decrypt() {
        String encryptedData = this.plainText, password = this.password;
        try {
            byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);

            // 1. Data ko tod kar Salt, IV aur CipherText nikalna
            byte[] salt = new byte[SALT_LENGTH_BYTE];
            byte[] iv = new byte[IV_LENGTH_BYTE];
            int cipherTextLength = combined.length - SALT_LENGTH_BYTE - IV_LENGTH_BYTE;
            byte[] cipherText = new byte[cipherTextLength];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH_BYTE);
            System.arraycopy(combined, SALT_LENGTH_BYTE, iv, 0, IV_LENGTH_BYTE);
            System.arraycopy(combined, SALT_LENGTH_BYTE + IV_LENGTH_BYTE, cipherText, 0, cipherTextLength);

            // 2. Wahi same Salt use karke Key wapas banana
            byte[] keyBytes = deriveKey(password, salt);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            // 3. Cipher Initialize
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            // 4. Final Decrypt
            byte[] decryptedBytes = cipher.doFinal(cipherText);
            String fullResult = new String(decryptedBytes, StandardCharsets.UTF_8);

            // 5. 🚩 TERA LOGIC: Verification Check
            String expectedTag = getSHA256Hash(password);

            // Check karo ki string badi hai aur aakhir mein wahi hash hai
            if (fullResult.length() >= 64 && fullResult.endsWith(expectedTag)) {
                // Agar match hua, toh Tag (64 chars) hata kar asli data return karo
                return fullResult.substring(0, fullResult.length() - 64);
            } else {
                // Custom check fail hua
                return "WRONG PASSWORD";
            }

        } catch (AEADBadTagException e) {
            // GCM Mode ne khud pakad liya ki password galat hai ya data corrupt hai
            return "WRONG PASSWORD";
        } catch (Exception e) {
            // Koi aur error aaye (jaise invalid base64) tab bhi yehi return karna better hai
            return "WRONG PASSWORD";
        }
    }

    /**
     * 🛠 Dynamic PBKDF2 Key Derivation Function
     */
    private static byte[] deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory skf;

        // OS version check karna taaki purane phones par crash na ho
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Android 8.0 aur uske baad ke naye devices ke liye (High Security)
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } else {
            // Android 7.1 aur usse purane devices ke liye (Fallback Security)
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BIT);
        return skf.generateSecret(spec).getEncoded();
    }
    /**
     * 🔍 SHA-256 Hash Generator (For Verification)
     */
    private static String getSHA256Hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public String encrypt() {
//        try {
//
//            return encryption(text, password);
//
//        } catch (Exception e) {
//            return  "Error in Encrypt";
//        }
//
//    }
//
//    public String decrypt() {
//        if (text == null) return "No text available!";
//
//        try {
//            return decryption(text, password);
//        } catch (Exception e) {
//            return  "Error in Decrypt";
//        }
//    }





}