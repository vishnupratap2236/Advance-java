package com.shopsphere.web.customer;

import com.shopsphere.model.Address;
import com.shopsphere.model.User;
import com.shopsphere.service.AddressService;
import com.shopsphere.service.AuthService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * The customer account area: profile, password change and the address book.
 */
@WebServlet(name = "AccountServlet", urlPatterns = {"/account", "/account/*"})
public class AccountServlet extends BaseServlet {

    private final AuthService authService = new AuthService();
    private final AddressService addressService = new AddressService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = WebContext.currentUser(request);
            if (user == null) {
                redirect(request, response, "/login?redirect=/account");
                return;
            }
            String path = request.getRequestURI().substring(request.getContextPath().length());
            if (path.startsWith("/account/addresses")) {
                List<Address> addresses = addressService.list(user.getId());
                request.setAttribute("addresses", addresses);
                render(request, response, "addresses", "My addresses");
                return;
            }
            request.setAttribute("profile", user);
            render(request, response, "account", "My account");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load your account right now. Please try again in a moment.");
        }
    }

    @Override
 protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = WebContext.currentUser(request);
        if (user == null) {
            redirect(request, response, "/login");
            return;
        }
        String action = param(request, "action");
        try {
            switch (action) {
                case "updateProfile" -> {
                    String fullName = param(request, "fullName");
                    String phone = param(request, "phone");
                    if (authService.updateProfile(user.getId(), fullName, phone)) {
                        User refreshed = new User();
                        refreshed.setId(user.getId());
                        refreshed.setEmail(user.getEmail());
                        refreshed.setFullName(fullName);
                        refreshed.setPhone(phone);
                        refreshed.setRole(user.getRole());
                        refreshed.setStatus(user.getStatus());
                        request.getSession().setAttribute(WebContext.SESSION_USER, refreshed);
                        redirectWithSuccess(request, response, "/account",
                                "Your profile was updated.");
                    } else {
                        request.setAttribute("profile", user);
                        request.setAttribute("formError",
                                "Please check the name and phone number and try again.");
                        render(request, response, "account", "My account");
                    }
                }
                case "changePassword" -> {
                    String currentPassword = request.getParameter("currentPassword");
                    String newPassword = request.getParameter("newPassword");
                    String confirmPassword = request.getParameter("confirmPassword");
                    if (newPassword == null || !newPassword.equals(confirmPassword)) {
                        request.setAttribute("profile", user);
                        request.setAttribute("formError", "The two new passwords do not match.");
                        render(request, response, "account", "My account");
                        return;
                    }
                    if (authService.changePassword(user.getId(), currentPassword, newPassword)) {
                        redirectWithSuccess(request, response, "/account",
                                "Your password was changed.");
                    } else {
                        request.setAttribute("profile", user);
                        request.setAttribute("formError",
                                "The current password is wrong or the new password is too weak. "
                                        + "Use at least 8 characters with letters and digits.");
                        render(request, response, "account", "My account");
                    }
                }
                default -> redirect(request, response, "/account");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "We could not update your account.");
        }
    }
}
