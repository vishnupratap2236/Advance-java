package com.shopsphere.web.admin;

import com.shopsphere.model.Order;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.PageResult;
import com.shopsphere.service.OrderService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Administration of orders: listing, filtering and status changes.
 */
@WebServlet(name = "AdminOrderServlet", urlPatterns = {"/admin/orders", "/admin/orders/*"})
public class AdminOrderServlet extends BaseServlet {

    private static final int PAGE_SIZE = 15;

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                long id = paramLong(request, "id", -1);
                Optional<Order> order = orderService.findById(id);
                if (order.isEmpty()) {
                    showError(request, response, HttpServletResponse.SC_NOT_FOUND,
                            "The order could not be found.");
                    return;
                }
                request.setAttribute("order", order.get());
                render(request, response, "admin/order-detail", "Order " + order.get().getOrderNumber());
                return;
            }

            int page = Math.max(1, paramInt(request, "page", 1));
            String status = paramOrNull(request, "status");
            String search = paramOrNull(request, "q");
            PageResult<Order> orders = orderService.listAll(page, PAGE_SIZE, status, search);

            request.setAttribute("orders", orders);
            request.setAttribute("statusFilter", status == null ? "ALL" : status);
            request.setAttribute("statuses", OrderStatus.values());
            request.setAttribute("searchTerm", search);
            render(request, response, "admin/orders", "Orders");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "The orders could not be loaded. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"updateStatus".equals(param(request, "action"))) {
            redirect(request, response, "/admin/orders");
            return;
        }
        long id = paramLong(request, "id", -1);
        String statusValue = param(request, "status");
        String note = paramOrNull(request, "note");
        try {
            OrderStatus status = OrderStatus.fromString(statusValue);
            boolean updated = orderService.updateStatus(id, status, note, WebContext.currentUserId(request));
            if (updated) {
                redirectWithSuccess(request, response, "/admin/orders?id=" + id,
                        "The order status was changed to " + status.getLabel() + ".");
            } else {
                redirectWithError(request, response, "/admin/orders",
                        "The order status could not be changed.");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "The order status could not be changed.");
        }
    }
}
