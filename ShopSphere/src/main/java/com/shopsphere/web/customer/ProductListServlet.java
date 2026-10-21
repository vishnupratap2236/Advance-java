package com.shopsphere.web.customer;

import com.shopsphere.model.Category;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.service.CatalogService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * The catalogue: search, category filter, price range, sorting and pagination.
 */
@WebServlet(name = "ProductListServlet", urlPatterns = {"/products", "/category"})
public class ProductListServlet extends BaseServlet {

    private static final int PAGE_SIZE = 9;

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ProductQuery query = new ProductQuery();
            query.setSearch(paramOrNull(request, "q"));
            query.setSort(ProductQuery.Sort.fromString(request.getParameter("sort")));
            query.setPage(paramInt(request, "page", 1));
            query.setPageSize(PAGE_SIZE);
            query.setOnlyInStock("inStock".equalsIgnoreCase(request.getParameter("inStock")));

            String categoryParam = paramOrNull(request, "category");
            Long categoryId = null;
            String categorySlug = null;
            if (categoryParam != null) {
                if (categoryParam.chars().allMatch(Character::isDigit)) {
                    categoryId = Long.parseLong(categoryParam);
                } else {
                    categorySlug = categoryParam;
                    Category found = catalogService.findCategoryBySlug(categoryParam).orElse(null);
                    if (found != null) {
                        categoryId = found.getId();
                    }
                }
            }
            query.setCategoryId(categoryId);

            BigDecimal minPrice = parsePrice(request.getParameter("minPrice"));
            BigDecimal maxPrice = parsePrice(request.getParameter("maxPrice"));
            query.setMinPrice(minPrice);
            query.setMaxPrice(maxPrice);

            PageResult<Product> products = catalogService.search(query);
            List<Category> categories = catalogService.activeCategories();

            request.setAttribute("products", products);
            request.setAttribute("categories", categories);
            request.setAttribute("query", query);
            request.setAttribute("categoryId", categoryId);
            request.setAttribute("categorySlug", categorySlug);
            request.setAttribute("searchTerm", query.getSearch());
            request.setAttribute("sort", query.getSort());
            request.setAttribute("sortOptions", ProductQuery.Sort.values());
            request.setAttribute("minPrice", minPrice);
            request.setAttribute("maxPrice", maxPrice);
            request.setAttribute("inStockOnly", query.isOnlyInStock());

            if (categorySlug != null) {
                catalogService.findCategoryBySlug(categorySlug)
                        .ifPresent(c -> request.setAttribute("activeCategory", c));
            } else if (categoryId != null) {
                catalogService.findCategory(categoryId)
                        .ifPresent(c -> request.setAttribute("activeCategory", c));
            }

            Object activeCategory = request.getAttribute("activeCategory");
            String title = "All products";
            if (activeCategory instanceof Category category) {
                title = category.getName();
            } else if (query.getSearch() != null) {
                title = "Search results for \"" + query.getSearch() + "\"";
            }
            render(request, response, "products", title);
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not load the catalogue right now. Please try again in a moment.");
        }
    }

    private BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            return value.signum() < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
