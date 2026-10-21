package com.shopsphere.web.customer;

import com.shopsphere.model.CartView;
import com.shopsphere.service.CartService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The shopping cart: add items, change quantities, remove items and clear the cart.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends BaseServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long userId = WebContext.currentUserId(request);
            String couponCode = request.getParameter("coupon");
            CartView view = cartService.view(userId, couponCode);
            request.setAttribute("cart", view);
            request.setAttribute("couponCode", couponCode);
            render(request, response, "cart", "Shopping cart");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load your cart right now. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        String action = param(request, "action");
        try {
            switch (action) {
                case "add" -> {
                    long productId = paramLong(request, "productId", -1);
                    int quantity = paramInt(request, "quantity", 1);
                    if (productId <= 0) {
                        redirectWithError(request, response, "/cart", "Invalid product.");
                        return;
                    }
                    try {
                        cartService.addItem(userId, productId, quantity);
                        WebContext.setCartCount(request, cartService.itemCount(userId));
                        redirectWithSuccess(request, response, "/cart", "The item was added to your cart.");
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        redirectWithError(request, response, "/cart", e.getMessage());
                    }
                }
                case "update" -> {
                    long productId = paramLong(request, "productId", -1);
                    int quantity = paramInt(request, "quantity", 1);
                    if (productId > 0) {
                        cartService.updateQuantity(userId, productId, quantity);
                        WebContext.setCartCount(request, cartService.itemCount(userId));
                    }
                    redirectWithSuccess(request, response, "/cart", "The cart was updated.");
                }
                case "remove" -> {
                    long productId = paramLong(request, "productId", -1);
                    if (productId > 0) {
                        cartService.removeItem(userId, productId);
                        WebContext.setCartCount(request, cartService.itemCount(userId));
                    }
                    redirectWithSuccess(request, response, "/cart", "The item was removed from your cart.");
                }
                case "clear" -> {
                    cartService.clear(userId);
                    WebContext.setCartCount(request, 0);
                    redirectWithSuccess(request, response, "/cart", "Your cart is now empty.");
                }
                default -> redirect(request, response, "/cart");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "We could not update your cart.");
        }
    }
}
