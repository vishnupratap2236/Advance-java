package com.shopsphere.web.filter;

import com.shopsphere.util.WebContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Authorisation check for the administration area.
 *
 * <p>Customers that try to open an administration URL are rejected with HTTP 403;
 * they never see administration data.
 */
public class AdminFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            if (!WebContext.isAuthenticated(httpRequest)) {
                httpResponse.sendRedirect(httpRequest.getContextPath()
                        + "/login?redirect=" + java.net.URLEncoder.encode("/admin",
                        java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
            if (!WebContext.isAdmin(httpRequest)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRequest.setAttribute("errorMessage",
                        "You do not have permission to access the administration area.");
                httpRequest.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(httpRequest, httpResponse);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
