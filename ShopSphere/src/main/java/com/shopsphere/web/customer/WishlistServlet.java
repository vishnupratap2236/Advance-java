package com.shopsphere.web.customer;

import com.shopsphere.model.Product;
import com.shopsphere.service.WishlistService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * The wishlist of the signed in customer.
 */
@WebServlet(name = "WishlistServlet", urlPatterns = {"/wishlist"})
public class WishlistServlet extends BaseServlet {

    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long userId = WebContext.currentUserId(request);
            List<Product> products = wishlistService.list(userId);
            request.setAttribute("wishlist", products);
            render(request, response, "wishlist", "My wishlist");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load your wishlist right now. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        long productId = paramLong(request, "productId", -1);
        try {
            if (productId > 0) {
                boolean inWishlist = wishlistService.toggle(userId, productId);
                redirectWithSuccess(request, response, "/wishlist",
                        inWishlist ? "The product was added to your wishlist."
                                : "The product was removed from your wishlist.");
            } else {
                redirect(request, response, "/wishlist");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "Your wishlist could not be updated.");
        }
    }
}
