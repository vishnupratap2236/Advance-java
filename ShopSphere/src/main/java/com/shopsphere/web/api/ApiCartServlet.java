package com.shopsphere.web.api;

import com.shopsphere.model.CartItem;
import com.shopsphere.model.CartView;
import com.shopsphere.service.CartService;
import com.shopsphere.util.Json;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON endpoint for the shopping cart of the signed in customer.
 *
 * <pre>
 * GET  /api/cart
 * POST /api/cart   productId=1&amp;quantity=2   (header X-CSRF-Token required)
 * </pre>
 */
@WebServlet(name = "ApiCartServlet", urlPatterns = {"/api/cart"})
public class ApiCartServlet extends BaseServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            long userId = WebContext.currentUserId(request);
            CartView cart = cartService.view(userId, null);
            response.getWriter().write(toJson(cart));
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, "API request failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(Json.object("error", "The request could not be processed."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            long userId = WebContext.currentUserId(request);
            long productId = paramLong(request, "productId", -1);
            int quantity = paramInt(request, "quantity", 1);
            if (productId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(Json.object("error", "A product id is required."));
                return;
            }
            cartService.addItem(userId, productId, quantity);
            CartView cart = cartService.view(userId, null);
            WebContext.setCartCount(request, cart.getItemCount());
            response.getWriter().write(toJson(cart));
        } catch (IllegalStateException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write(Json.object("error", e.getMessage()));
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, "API request failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(Json.object("error", "The request could not be processed."));
        }
    }

    private String toJson(CartView cart) {
        List<String> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            items.add(Json.object(
                    "productId", String.valueOf(item.getProductId()),
                    "name", item.getProduct() == null ? "" : item.getProduct().getName(),
                    "price", item.getProduct() == null ? "0" : item.getProduct().getPrice().toPlainString(),
                    "quantity", String.valueOf(item.getQuantity()),
                    "lineTotal", item.getLineTotal().toPlainString(),
                    "inStock", String.valueOf(item.isAvailable())));
        }
        return Json.object(
                "itemCount", String.valueOf(cart.getItemCount()),
                "subtotal", cart.getSubtotal().toPlainString(),
                "discount", cart.getDiscount().toPlainString(),
                "shippingFee", cart.getShippingFee().toPlainString(),
                "tax", cart.getTax().toPlainString(),
                "total", cart.getTotal().toPlainString(),
                "items", Json.array(items));
    }
}
