package com.shopsphere.web.customer;

import com.shopsphere.model.User;
import com.shopsphere.service.AuthService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * New customer registration. Customers are created with the CUSTOMER role only;
 * administrator accounts are provisioned separately.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (WebContext.isAuthenticated(request)) {
            redirect(request, response, "/");
            return;
        }
        render(request, response, "register", "Create an account");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = param(request, "email");
        String fullName = param(request, "fullName");
        String phone = param(request, "phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        AuthService.RegistrationResult result =
                authService.register(email, password, confirmPassword, fullName, phone);
        if (!result.success()) {
            request.setAttribute("errorMessage", result.error());
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            render(request, response, "register", "Create an account");
            return;
        }

        User user = result.user();
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(WebContext.SESSION_USER, user);
        WebContext.flashSuccess(request, "Your account is ready. Welcome, " + user.getFullName() + "!");
        redirect(request, response, "/");
    }
}
