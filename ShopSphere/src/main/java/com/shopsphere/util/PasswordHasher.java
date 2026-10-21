package com.shopsphere.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing helpers based on BCrypt.
 *
 * <p>Plaintext passwords are never stored. Each hash uses a random salt, so two
 * accounts with the same password produce different hashes.
 */
public final class PasswordHasher {

    private static final int WORK_FACTOR = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        String salt = BCrypt.gensalt(WORK_FACTOR, RANDOM);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @return {@code true} when the password matches
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Stored value is not a valid BCrypt hash.
            return false;
        }
    }

    /**
     * Checks whether a stored hash was created with a weaker work factor than the
     * current policy and should be re-hashed on the next successful login.
     */
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2")) {
            return true;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length < 3) {
            return true;
        }
        try {
            return Integer.parseInt(parts[2]) < WORK_FACTOR;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Generates a cryptographically strong random password.
     *
     * <p>The result always contains at least one lower case letter, one upper case
     * letter, one digit and one special character.
     */
    public static String randomPassword(int length) {
        final String lower = "abcdefghijkmnopqrstuvwxyz";
        final String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        final String digits = "23456789";
        final String special = "!@#$%";
        final String all = lower + upper + digits + special;

        StringBuilder sb = new StringBuilder(length);
        sb.append(lower.charAt(RANDOM.nextInt(lower.length())));
        sb.append(upper.charAt(RANDOM.nextInt(upper.length())));
        sb.append(digits.charAt(RANDOM.nextInt(digits.length())));
        sb.append(special.charAt(RANDOM.nextInt(special.length())));
        while (sb.length() < length) {
            sb.append(all.charAt(RANDOM.nextInt(all.length())));
        }
        // Shuffle so that the guaranteed characters are not always in front.
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    /** Generates a URL-safe random token, used for CSRF tokens and order numbers. */
    public static String randomToken(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}
