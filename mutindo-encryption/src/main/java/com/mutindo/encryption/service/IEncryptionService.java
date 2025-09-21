package com.mutindo.encryption.service;

/**
 * Encryption service interface for polymorphic cryptographic operations
 * Follows our established pattern of interface-driven design
 */
public interface IEncryptionService {
    
    /**
     * Hash password using BCrypt
     * @param plainPassword Plain text password
     * @return BCrypt hashed password
     */
    String hashPassword(String plainPassword);
    
    /**
     * Verify password against hash
     * @param plainPassword Plain text password
     * @param hashedPassword BCrypt hashed password
     * @return true if password matches
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);
    
    /**
     * Encrypt sensitive data using AES-GCM
     * @param plainText Data to encrypt
     * @return Base64 encoded encrypted data
     */
    String encryptData(String plainText);
    
    /**
     * Decrypt sensitive data
     * @param encryptedText Base64 encoded encrypted data
     * @return Decrypted plain text
     */
    String decryptData(String encryptedText);
    
    /**
     * Generate new master key for key rotation
     * @return Base64 encoded master key
     */
    String generateNewMasterKey();
}
