package com.mutindo.encryption.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mutindo.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption service implementation - focused only on cryptographic operations
 * Follows our established interface-driven pattern
 */
@Service
@Slf4j
public class EncryptionService implements IEncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${cbs.encryption.master-key}")
    private String masterKey;

    /**
     * Hash password using BCrypt - for password storage
     */
    public String hashPassword(String plainPassword) {
        try {
            return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
        } catch (Exception e) {
            log.error("Failed to hash password", e);
            throw new BusinessException("Password hashing failed", "ENCRYPTION_ERROR");
        }
    }

    /**
     * Verify password against hash
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            return result.verified;
        } catch (Exception e) {
            log.error("Failed to verify password", e);
            return false;
        }
    }

    /**
     * Encrypt sensitive data using AES-GCM - for PII, biometric templates, etc.
     */
    public String encryptData(String plainText) {
        try {
            SecretKey secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(masterKey), AES_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new BusinessException("Data encryption failed", "ENCRYPTION_ERROR");
        }
    }

    /**
     * Decrypt sensitive data
     */
    public String decryptData(String encryptedText) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            SecretKey secretKey = new SecretKeySpec(
                Base64.getDecoder().decode(masterKey), AES_ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new BusinessException("Data decryption failed", "DECRYPTION_ERROR");
        }
    }

    /**
     * Generate a new AES key for master key rotation
     */
    public String generateNewMasterKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate master key", e);
            throw new BusinessException("Key generation failed", "KEY_GENERATION_ERROR");
        }
    }
}
