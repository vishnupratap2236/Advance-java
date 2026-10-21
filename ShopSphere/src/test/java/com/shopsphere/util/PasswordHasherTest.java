package com.shopsphere.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    @DisplayName("a password can be verified against its hash")
    void hashesAndVerifies() {
        String hash = PasswordHasher.hash("Str0ngPassw0rd");

        assertTrue(PasswordHasher.matches("Str0ngPassw0rd", hash));
        assertFalse(PasswordHasher.matches("wrong-password", hash));
    }

    @Test
    @DisplayName("the hash never contains the plaintext password")
    void hashDoesNotContainPlaintext() {
        String password = "MySecret123!";
        String hash = PasswordHasher.hash(password);

        assertFalse(hash.contains(password));
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    @DisplayName("the same password produces a different hash every time (random salt)")
    void usesRandomSalt() {
        String first = PasswordHasher.hash("SamePassword1");
        String second = PasswordHasher.hash("SamePassword1");

        assertNotEquals(first, second);
        assertTrue(PasswordHasher.matches("SamePassword1", first));
        assertTrue(PasswordHasher.matches("SamePassword1", second));
    }

    @Test
    @DisplayName("invalid input is rejected instead of throwing")
    void rejectsInvalidInput() {
        assertFalse(PasswordHasher.matches(null, "$2a$12$abcdefghijklmnopqrstuv"));
        assertFalse(PasswordHasher.matches("secret", null));
        assertFalse(PasswordHasher.matches("secret", "not-a-hash"));
    }

    @Test
    @DisplayName("weak hashes are flagged for rehashing")
    void detectsWeakHashes() {
        assertTrue(PasswordHasher.needsRehash(null));
        assertTrue(PasswordHasher.needsRehash("plaintext"));
        assertFalse(PasswordHasher.needsRehash(PasswordHasher.hash("Whatever123")));
    }

    @Test
    @DisplayName("generated passwords are long enough and random")
    void generatesRandomPasswords() {
        String password = PasswordHasher.randomPassword(18);

        assertEquals(18, password.length());
        assertNotEquals(password, PasswordHasher.randomPassword(18));
        assertTrue(Validation.isPasswordStrong(password));
    }
}
