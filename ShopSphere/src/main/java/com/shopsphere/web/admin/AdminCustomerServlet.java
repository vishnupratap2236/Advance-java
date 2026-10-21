package com.shopsphere.web.admin;

import com.shopsphere.model.PageResult;
import com.shopsphere.model.User;
import com.shopsphere.service.AdminService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The customer list of the administration area.
 */
@WebServlet(name = "AdminCustomerServlet", urlPatterns = {"/admin/customers"})
public class AdminCustomerServlet extends BaseServlet {

    private static final int PAGE_SIZE = 15;

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int page = Math.max(1, paramInt(request, "page", 1));
            PageResult<User> customers = adminService.listCustomers(page, PAGE_SIZE);
            request.setAttribute("customers", customers);
            render(request, response, "admin/customers", "Customers");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "The customer list could not be loaded. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"setStatus".equals(param(request, "action"))) {
            redirect(request, response, "/admin/customers");
            return;
        }
        long id = paramLong(request, "id", -1);
        String status = param(request, "status");
        try {
            boolean updated = adminService.setCustomerStatus(id, status);
            if (updated) {
                redirectWithSuccess(request, response, "/admin/customers",
                        "The customer account was updated.");
            } else {
                redirectWithError(request, response, "/admin/customers",
                        "The customer account could not be updated.");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "The customer account could not be updated.");
        }
    }
}
