package com.shopsphere.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Forces the UTF-8 character encoding for requests and responses.
 */
public class CharacterEncodingFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    /** Kept to satisfy the Filter contract; nothing to release. */
    @Override
    public void destroy() {
        // nothing to clean up
    }

    @SuppressWarnings("unused")
    private void unusedSessionReference(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("encoding", "UTF-8");
        }
    }

    @SuppressWarnings("unused")
    private void unusedResponseReference(HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
    }
}
