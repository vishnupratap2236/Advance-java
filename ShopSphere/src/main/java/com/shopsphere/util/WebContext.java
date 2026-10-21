package com.shopsphere.util;

import com.shopsphere.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Session helpers: the signed in user, flash messages and cart badge count.
 */
public final class WebContext {

    public static final String SESSION_USER = "auth.user";
    public static final String SESSION_CART_COUNT = "cart.count";
    public static final String SESSION_FLASH = "flash.messages";
    public static final String SESSION_REDIRECT_AFTER_LOGIN = "redirect.afterLogin";

    private WebContext() {
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(SESSION_USER);
        return user instanceof User u ? u : null;
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return currentUser(request) != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = currentUser(request);
        return user != null && user.isAdmin();
    }

    public static long currentUserId(HttpServletRequest request) {
        User user = currentUser(request);
        return user == null ? 0L : user.getId();
    }

    /** Stores a message that survives one redirect. */
    public static void flash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession(true);
        synchronized (session) {
            @SuppressWarnings("unchecked")
            List<String[]> messages = (List<String[]>) session.getAttribute(SESSION_FLASH);
            if (messages == null) {
                messages = new ArrayList<>();
                session.setAttribute(SESSION_FLASH, messages);
            }
            messages.add(new String[]{type, message});
        }
    }

    public static void flashSuccess(HttpServletRequest request, String message) {
        flash(request, "success", message);
    }

    public static void flashError(HttpServletRequest request, String message) {
        flash(request, "danger", message);
    }

    public static void flashInfo(HttpServletRequest request, String message) {
        flash(request, "info", message);
    }

    /** Returns and clears the pending flash messages. */
    @SuppressWarnings("unchecked")
    public static List<String[]> consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return List.of();
        }
        synchronized (session) {
            List<String[]> messages = (List<String[]>) session.getAttribute(SESSION_FLASH);
            session.removeAttribute(SESSION_FLASH);
            return messages == null ? List.of() : messages;
        }
    }

    public static void setCartCount(HttpServletRequest request, int count) {
        request.getSession(true).setAttribute(SESSION_CART_COUNT, count);
    }

    public static int cartCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return 0;
        }
        Object count = session.getAttribute(SESSION_CART_COUNT);
        return count instanceof Integer i ? i : 0;
    }
}
