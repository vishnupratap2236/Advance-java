package com.shopsphere.web.customer;

import com.shopsphere.service.ReviewService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles the customer reviews of a product.
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/review"})
public class ReviewServlet extends BaseServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        if (userId == 0) {
            redirectWithError(request, response, "/login", "Please sign in to write a review.");
            return;
        }
        String action = param(request, "action");
        long productId = paramLong(request, "productId", -1);
        try {
            switch (action) {
                case "add" -> {
                    int rating = paramInt(request, "rating", 0);
                    String title = paramOrNull(request, "title");
                    String comment = paramOrNull(request, "comment");
                    ReviewService.ReviewResult result =
                            reviewService.add(productId, userId, rating, title, comment);
                    if (result.success()) {
                        redirectWithSuccess(request, response, "/product?id=" + productId,
                                "Thank you for your review.");
                    } else {
                        WebContext.flashError(request, result.error());
                        redirect(request, response, "/product?id=" + productId);
                    }
                }
                case "delete" -> {
                    long reviewId = paramLong(request, "reviewId", -1);
                    if (reviewService.delete(reviewId, userId)) {
                        redirectWithSuccess(request, response, "/product?id=" + productId,
                                "The review was deleted.");
                    } else {
                        redirectWithError(request, response, "/product?id=" + productId,
                                "The review could not be deleted.");
                    }
                }
                default -> redirect(request, response, "/products");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "The review could not be saved.");
        }
    }
}
