package com.example.keycloak.storage.database;

import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Hasher - handles password hashing and verification
 * Supports both BCrypt and SHA-256 for compatibility with existing systems
 */
public class PasswordHasher {
    private static final Logger logger = Logger.getLogger(PasswordHasher.class);
    private static final String BCRYPT_PREFIX = "$2a$";
    private static final String SHA256_PREFIX = "{SHA256}";

    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            if (hashedPassword.startsWith(BCRYPT_PREFIX)) {
                return verifyBCrypt(plainPassword, hashedPassword);
            } else if (hashedPassword.startsWith(SHA256_PREFIX)) {
                return verifySHA256(plainPassword, hashedPassword);
            } else {
                // Try direct comparison for plain text (development only)
                logger.warn("Plain text password detected - not secure for production!");
                return plainPassword.equals(hashedPassword);
            }
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Verify BCrypt password (using simple BCrypt implementation)
     */
    private static boolean verifyBCrypt(String plainPassword, String hashedPassword) {
        try {
            // For production, use a proper BCrypt library like jBCrypt or Spring Security BCrypt
            // This is a simplified version for POC
            return BCryptSimple.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("BCrypt verification failed", e);
            return false;
        }
    }

    /**
     * Verify SHA-256 password
     */
    private static boolean verifySHA256(String plainPassword, String hashedPassword) {
        try {
            String computedHash = hashWithSHA256(plainPassword);
            return hashedPassword.equals(computedHash);
        } catch (Exception e) {
            logger.error("SHA-256 verification failed", e);
            return false;
        }
    }

    /**
     * Hash password with SHA-256
     */
    public static String hashWithSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            String encoded = Base64.getEncoder().encodeToString(hash);
            return SHA256_PREFIX + encoded;
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not found", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Simplified BCrypt implementation for POC
     * In production, use org.mindrot.jbcrypt.BCrypt or similar library
     */
    private static class BCryptSimple {
        public static boolean checkpw(String plaintext, String hashed) {
            // Simplified BCrypt check - in production use proper BCrypt library
            // For POC, we'll use SHA-256 as fallback
            try {
                // Extract salt from hashed password (BCrypt format: $2a$rounds$salt+hash)
                String[] parts = hashed.split("\\$");
                if (parts.length < 4) {
                    return false;
                }

                // For POC: simple comparison (replace with proper BCrypt in production)
                String testHash = hashSimple(plaintext, parts[3].substring(0, 22));
                return hashed.equals(testHash);
            } catch (Exception e) {
                logger.error("BCrypt check failed", e);
                return false;
            }
        }

        private static String hashSimple(String password, String salt) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt.getBytes());
                byte[] hash = md.digest(password.getBytes());
                String encoded = Base64.getEncoder().encodeToString(hash);
                return "$2a$10$" + salt + encoded.substring(0, 31);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Generate BCrypt hash (for testing/setup)
     */
    public static String hashPassword(String password) {
        // For POC, use SHA-256
        // In production, replace with proper BCrypt
        return hashWithSHA256(password);
    }
}
