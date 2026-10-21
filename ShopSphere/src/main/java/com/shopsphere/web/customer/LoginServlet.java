package com.shopsphere.web.customer;

import com.shopsphere.model.User;
import com.shopsphere.service.AuthService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Customer sign in.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (WebContext.isAuthenticated(request)) {
            redirect(request, response, "/");
            return;
        }
        request.setAttribute("redirectTo", sanitiseRedirect(request.getParameter("redirect")));
        render(request, response, "login", "Sign in");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = param(request, "email");
        String password = request.getParameter("password");
        String redirectTo = sanitiseRedirect(paramOrNull(request, "redirectTo"));
        String ip = clientIp(request);

        if (email.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Enter your email address and your password.");
            request.setAttribute("email", email);
            render(request, response, "login", "Sign in");
            return;
        }

        AuthService.LoginResult result = authService.login(email, password, ip);
        if (!result.success()) {
            request.setAttribute("errorMessage", result.error());
            request.setAttribute("email", email);
            render(request, response, "login", "Sign in");
            return;
        }

        User user = result.user();
        HttpSession session = request.getSession();
        // Renew the session identifier to prevent session fixation.
        request.changeSessionId();
        session.setAttribute(WebContext.SESSION_USER, user);

        if (redirectTo == null) {
            Object stored = session.getAttribute(WebContext.SESSION_REDIRECT_AFTER_LOGIN);
            if (stored instanceof String storedRedirect) {
                redirectTo = sanitiseRedirect(storedRedirect);
                session.removeAttribute(WebContext.SESSION_REDIRECT_AFTER_LOGIN);
            }
        }
        String target = redirectTo != null ? redirectTo : (user.isAdmin() ? "/admin" : "/");
        WebContext.flashSuccess(request, "Welcome back, " + user.getFullName() + "!");
        redirect(request, response, target);
    }

    /** Only relative paths inside the application are accepted as a redirect target. */
    static String sanitiseRedirect(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        if (!candidate.startsWith("/") || candidate.startsWith("//")) {
            return null;
        }
        if (candidate.contains("\r") || candidate.contains("\n") || candidate.contains(":")) {
            return null;
        }
        return candidate;
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
