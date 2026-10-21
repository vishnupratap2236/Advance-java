package com.shopsphere.web.filter;

import com.shopsphere.util.WebContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Central authentication check for the customer area.
 *
 * <p>Requests to protected paths must carry a valid session. Unauthenticated visitors
 * are redirected to the login page instead of seeing a partially rendered page.
 */
public class AuthenticationFilter implements jakarta.servlet.Filter {

    private static final List<String> PROTECTED_PREFIXES = List.of(
            "/account", "/checkout", "/orders", "/order", "/cart", "/wishlist", "/api/cart");

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (isProtected(path) && !WebContext.isAuthenticated(httpRequest)) {
            if (path.startsWith("/api/")) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"error\":\"Authentication required\"}");
                return;
            }
            String target = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            if (httpRequest.getQueryString() != null) {
                target = target + "?" + httpRequest.getQueryString();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath()
                    + "/login?redirect=" + java.net.URLEncoder.encode(target, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isProtected(String path) {
        for (String prefix : PROTECTED_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }
}
