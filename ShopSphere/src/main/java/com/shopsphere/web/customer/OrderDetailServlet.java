package com.shopsphere.web.customer;

import com.shopsphere.model.Order;
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
 * The detail view of a single order.
 *
 * <p>The order is only shown when it belongs to the signed in customer, which
 * prevents a customer from reading the orders of somebody else by guessing an id.
 */
@WebServlet(name = "OrderDetailServlet", urlPatterns = {"/order", "/order/*"})
public class OrderDetailServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long userId = WebContext.currentUserId(request);
            long orderId = paramLong(request, "id", -1);
            if (orderId <= 0) {
                showError(request, response, HttpServletResponse.SC_NOT_FOUND, "Order not found.");
                return;
            }
            Optional<Order> order = orderService.findForUser(orderId, userId);
            if (order.isEmpty()) {
                showError(request, response, HttpServletResponse.SC_NOT_FOUND,
                        "We could not find that order in your account.");
                return;
            }
            request.setAttribute("order", order.get());
            if (request.getRequestURI().endsWith("/success")) {
                render(request, response, "order-success", "Order confirmed");
            } else {
                render(request, response, "order-detail", "Order " + order.get().getOrderNumber());
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load this order right now. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        if (!"cancel".equals(param(request, "action"))) {
            redirect(request, response, "/orders");
            return;
        }
        long orderId = paramLong(request, "id", -1);
        try {
            boolean cancelled = orderService.cancelByCustomer(orderId, userId);
            if (cancelled) {
                redirectWithSuccess(request, response, "/order?id=" + orderId,
                        "The order was cancelled.");
            } else {
                redirectWithError(request, response, "/orders",
                        "This order can no longer be cancelled. Please contact support.");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "We could not cancel the order.");
        }
    }
}
