package com.shopsphere.web.filter;

import com.shopsphere.util.Csrf;
import com.shopsphere.util.WebContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Rejects state changing requests that do not carry a valid CSRF token.
 *
 * <p>The token is bound to the session and is submitted with the form or, for AJAX
 * calls, in the {@code X-CSRF-Token} header.
 */
public class CsrfFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        if (isStateChanging(method)) {
            String submitted = httpRequest.getParameter(Csrf.PARAMETER_NAME);
            if (submitted == null || submitted.isBlank()) {
                submitted = httpRequest.getHeader(Csrf.HEADER_NAME);
            }
            if (!Csrf.isValid(httpRequest, submitted)) {
                if (isAjax(httpRequest)) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write(
                            "{\"error\":\"Invalid or missing CSRF token. Please reload the page.\"}");
                    return;
                }
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRequest.setAttribute("errorMessage",
                        "Your session expired or the form was tampered with. Please try again.");
                httpRequest.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(httpRequest, httpResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isStateChanging(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || request.getRequestURI().startsWith(request.getContextPath() + "/api/");
    }

    @SuppressWarnings("unused")
    private void unusedReference(HttpServletRequest request) {
        WebContext.currentUser(request);
    }
}
