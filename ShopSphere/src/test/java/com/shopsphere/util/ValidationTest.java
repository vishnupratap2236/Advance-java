package com.shopsphere.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    @Test
    @DisplayName("valid e-mail addresses are accepted")
    void acceptsValidEmails() {
        assertTrue(Validation.isEmail("shyam@example.com"));
        assertTrue(Validation.isEmail("first.last+tag@sub.domain.co.in"));
    }

    @Test
    @DisplayName("invalid e-mail addresses are rejected")
    void rejectsInvalidEmails() {
        assertFalse(Validation.isEmail("no-at-sign"));
        assertFalse(Validation.isEmail("spaces in@mail.com"));
        assertFalse(Validation.isEmail("@example.com"));
        assertFalse(Validation.isEmail(null));
    }

    @Test
    @DisplayName("passwords need at least eight characters, letters and digits")
    void enforcesStrongPasswords() {
        assertTrue(Validation.isPasswordStrong("abcd1234"));
        assertFalse(Validation.isPasswordStrong("short1"));
        assertFalse(Validation.isPasswordStrong("onlyletters"));
        assertFalse(Validation.isPasswordStrong("12345678"));
        assertFalse(Validation.isPasswordStrong(null));
    }

    @Test
    @DisplayName("phone numbers are validated loosely but not empty")
    void validatesPhoneNumbers() {
        assertTrue(Validation.isPhone("+91 98765 43210"));
        assertTrue(Validation.isPhone("9876543210"));
        assertFalse(Validation.isPhone("call-me"));
        assertFalse(Validation.isPhone(""));
    }

    @Test
    @DisplayName("slugs are generated in a URL friendly way")
    void createsSlugs() {
        assertEquals("aurora-wireless-headphones", Validation.slugify("Aurora Wireless Headphones"));
        assertEquals("tea-set", Validation.slugify("  Tea & Set  "));
        assertEquals("item", Validation.slugify(""));
    }

    @Test
    @DisplayName("HTML is escaped so that it cannot break out of a page")
    void escapesHtml() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
                Validation.escapeHtml("<script>alert(1)</script>"));
        assertEquals("Tom &amp; Jerry", Validation.escapeHtml("Tom & Jerry"));
        assertEquals("&quot;quoted&quot;", Validation.escapeHtml("\"quoted\""));
    }

    @Test
    @DisplayName("numbers are parsed defensively")
    void parsesNumbers() {
        assertEquals(5, Validation.parseInt("5", 0));
        assertEquals(7, Validation.parseInt("abc", 7));
        assertEquals(3L, Validation.parseLong("3", 0));
        assertEquals(-1L, Validation.parseLong("x", -1));
    }
}
