package com.shopsphere.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Session bound CSRF protection for state changing requests.
 *
 * <p>A random token is created per session and must be echoed back in the
 * {@code _csrf} parameter (or the {@code X-CSRF-Token} header for AJAX calls).
 */
public final class Csrf {

    public static final String SESSION_ATTRIBUTE = "csrfToken";
    public static final String PARAMETER_NAME = "_csrf";
    public static final String HEADER_NAME = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() {
    }

    /** Returns the token of the current session, creating it when necessary. */
    public static String token(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        synchronized (session.getId().intern()) {
            Object token = session.getAttribute(SESSION_ATTRIBUTE);
            if (token instanceof String existing && !existing.isBlank()) {
                return existing;
            }
            String created = newToken();
            session.setAttribute(SESSION_ATTRIBUTE, created);
            return created;
        }
    }

    public static String newToken() {
        byte[] buffer = new byte[32];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    /**
     * Constant time comparison of the submitted token against the session token.
     */
    public static boolean isValid(HttpServletRequest request, String submitted) {
        if (submitted == null || submitted.isBlank()) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTRIBUTE);
        if (!(expected instanceof String expectedToken) || expectedToken.isBlank()) {
            return false;
        }
        byte[] a = expectedToken.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] b = submitted.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(a, b);
    }
}
