package com.shopsphere.web;

import com.shopsphere.config.AppConfig;
import com.shopsphere.model.User;
import com.shopsphere.util.Csrf;
import com.shopsphere.util.WebContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared behaviour of all controllers: view rendering, redirects, safe parameter
 * access and a uniform error handling that never leaks stack traces to the browser.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    protected static final String VIEW_PREFIX = "/WEB-INF/views/";

    protected void render(HttpServletRequest request, HttpServletResponse response, String view,
                          String pageTitle) throws ServletException, IOException {
        prepare(request, pageTitle);
        request.getRequestDispatcher(VIEW_PREFIX + view + ".jsp").forward(request, response);
    }

    protected void prepare(HttpServletRequest request, String pageTitle) {
        request.setAttribute("pageTitle", pageTitle == null ? AppConfig.get().appName() : pageTitle);
        request.setAttribute("appName", AppConfig.get().appName());
        request.setAttribute("currencySymbol", AppConfig.get().currencySymbol());
        request.setAttribute("csrfToken", Csrf.token(request));
        User user = WebContext.currentUser(request);
        request.setAttribute("currentUser", user);
        request.setAttribute("cartCount", WebContext.cartCount(request));
        request.setAttribute("flashMessages", WebContext.consumeFlash(request));
        request.setAttribute("contextPath", request.getContextPath());
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirectWithSuccess(HttpServletRequest request, HttpServletResponse response,
                                       String path, String message) {
        WebContext.flashSuccess(request, message);
        try {
            redirect(request, response, path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not redirect", e);
        }
    }

    protected void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                     String path, String message) {
        WebContext.flashError(request, message);
        try {
            redirect(request, response, path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not redirect", e);
        }
    }

    protected String param(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    protected String paramOrNull(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected boolean paramBoolean(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value != null && (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")
                || value.equals("1"));
    }

    protected long paramLong(HttpServletRequest request, String name, long fallback) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    protected int paramInt(HttpServletRequest request, String name, int fallback) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Renders the shared error page without exposing internal details. */
    protected void showError(HttpServletRequest request, HttpServletResponse response, int status,
                             String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(VIEW_PREFIX + "error.jsp").forward(request, response);
    }

    protected void handleUnexpected(HttpServletRequest request, HttpServletResponse response,
                                    Exception e, String userMessage) throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Unexpected error while handling " + request.getRequestURI(), e);
        showError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, userMessage);
    }
}
