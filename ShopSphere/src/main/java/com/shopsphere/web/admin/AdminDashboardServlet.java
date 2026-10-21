package com.shopsphere.web.admin;

import com.shopsphere.model.DashboardStats;
import com.shopsphere.service.AdminService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The administration dashboard with the key figures of the shop.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin", "/admin/"})
public class AdminDashboardServlet extends BaseServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            DashboardStats stats = adminService.dashboard();
            request.setAttribute("stats", stats);
            render(request, response, "admin/dashboard", "Administration");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "The dashboard could not be loaded. Please try again in a moment.");
        }
    }
}
