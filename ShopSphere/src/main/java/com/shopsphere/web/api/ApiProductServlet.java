package com.shopsphere.web.api;

import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.service.CatalogService;
import com.shopsphere.util.Json;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read only JSON endpoint for the catalogue.
 *
 * <pre>
 * GET /api/products?q=watch&amp;category=audio&amp;sort=price_asc&amp;page=1&amp;pageSize=9
 * </pre>
 */
@WebServlet(name = "ApiProductServlet", urlPatterns = {"/api/products"})
public class ApiProductServlet extends BaseServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            ProductQuery query = new ProductQuery();
            query.setSearch(paramOrNull(request, "q"));
            query.setSort(ProductQuery.Sort.fromString(request.getParameter("sort")));
            query.setPage(Math.max(1, paramInt(request, "page", 1)));
            query.setPageSize(Math.min(48, Math.max(1, paramInt(request, "pageSize", 12))));
            String category = paramOrNull(request, "category");
            if (category != null && category.chars().allMatch(Character::isDigit)) {
                query.setCategoryId(Long.parseLong(category));
            } else if (category != null) {
                CategoryId categoryId = CategoryId.of(catalogService, category);
                query.setCategoryId(categoryId.value());
            }

            PageResult<Product> result = catalogService.search(query);

            List<String> items = new ArrayList<>();
            for (Product product : result.getItems()) {
                items.add(Json.object(
                        "id", String.valueOf(product.getId()),
                        "name", product.getName(),
                        "slug", product.getSlug(),
                        "category", product.getCategoryName() == null ? "" : product.getCategoryName(),
                        "price", product.getPrice().toPlainString(),
                        "comparePrice", product.getComparePrice() == null
                                ? "null" : product.getComparePrice().toPlainString(),
                        "stock", String.valueOf(product.getStock()),
                        "inStock", String.valueOf(product.isInStock()),
                        "imageUrl", product.getImageUrl() == null ? "" : product.getImageUrl(),
                        "rating", product.getRating().toPlainString()));
            }
            response.getWriter().write(Json.object(
                    "page", String.valueOf(result.getPage()),
                    "pageSize", String.valueOf(result.getPageSize()),
                    "totalItems", String.valueOf(result.getTotalItems()),
                    "totalPages", String.valueOf(result.getTotalPages()),
                    "items", Json.array(items)));
        } catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, "API request failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(Json.object("error", "The request could not be processed."));
        }
    }

    /** Small helper that resolves a category slug to its identifier. */
    private record CategoryId(Long value) {
        static CategoryId of(CatalogService service, String slug) {
            try {
                return new CategoryId(service.findCategoryBySlug(slug)
                        .map(c -> c.getId())
                        .orElse(null));
            } catch (java.sql.SQLException e) {
                return new CategoryId(null);
            }
        }
    }
}
