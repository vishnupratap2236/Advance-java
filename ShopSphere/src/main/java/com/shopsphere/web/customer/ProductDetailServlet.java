package com.shopsphere.web.customer;

import com.shopsphere.model.Product;
import com.shopsphere.service.CatalogService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The product detail page with image gallery, stock information and related products.
 */
@WebServlet(name = "ProductDetailServlet", urlPatterns = {"/product", "/product/*"})
public class ProductDetailServlet extends BaseServlet {

    private final CatalogService catalogService = new CatalogService();
    private final com.shopsphere.service.ReviewService reviewService =
            new com.shopsphere.service.ReviewService();
    private final com.shopsphere.service.WishlistService wishlistService =
            new com.shopsphere.service.WishlistService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            String slug = request.getParameter("slug");
            Optional<Product> product = Optional.empty();
            if (idParam != null && !idParam.isBlank()) {
                long id = paramLong(request, "id", -1);
                if (id > 0) {
                    product = catalogService.findProduct(id);
                }
            } else if (slug != null && !slug.isBlank()) {
                product = catalogService.findProductBySlug(slug);
            }
            if (product.isEmpty()) {
                showError(request, response, HttpServletResponse.SC_NOT_FOUND,
                        "The product you are looking for is not available.");
                return;
            }
            List<Product> related = catalogService.relatedProducts(product.get(), 4);
            request.setAttribute("product", product.get());
            request.setAttribute("relatedProducts", related);
            request.setAttribute("reviews", reviewService.listForProduct(product.get().getId()));

            long userId = com.shopsphere.util.WebContext.currentUserId(request);
            if (userId > 0) {
                reviewService.findOwnReview(userId, product.get().getId())
                        .ifPresent(review -> request.setAttribute("ownReview", review));
                request.setAttribute("inWishlist", wishlistService.contains(userId, product.get().getId()));
            }
            render(request, response, "product-detail", product.get().getName());
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load this product right now. Please try again in a moment.");
        }
    }
}
