package com.shopsphere.web.customer;

import com.shopsphere.model.Category;
import com.shopsphere.model.Product;
import com.shopsphere.service.CatalogService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * The landing page with featured categories, featured products and new arrivals.
 *
 * <p>The empty string pattern maps the servlet to the context root only. The
 * pattern {@code "/"} would be the default servlet and would swallow every request
 * that is not handled by another servlet, including the static resources.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Category> categories = catalogService.activeCategories();
            List<Product> featured = catalogService.featuredProducts(8);
            List<Product> latest = catalogService.latestProducts(4);

            request.setAttribute("categories", categories);
            request.setAttribute("featuredProducts", featured);
            request.setAttribute("latestProducts", latest);
            request.setAttribute("totalProducts", featured.size() + latest.size());
            render(request, response, "home", "Welcome to ShopSphere");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load the shop right now. Please try again in a moment.");
        }
    }
}
