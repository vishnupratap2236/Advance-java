package com.shopsphere.web.customer;

import com.shopsphere.model.Order;
import com.shopsphere.model.PageResult;
import com.shopsphere.service.OrderService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The order history of the signed in customer.
 */
@WebServlet(name = "OrderListServlet", urlPatterns = {"/orders"})
public class OrderListServlet extends BaseServlet {

    private static final int PAGE_SIZE = 8;

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long userId = WebContext.currentUserId(request);
            int page = Math.max(1, paramInt(request, "page", 1));
            PageResult<Order> orders = orderService.listForUser(userId, page, PAGE_SIZE);
            request.setAttribute("orders", orders);
            render(request, response, "orders", "My orders");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load your orders right now. Please try again in a moment.");
        }
    }
}
