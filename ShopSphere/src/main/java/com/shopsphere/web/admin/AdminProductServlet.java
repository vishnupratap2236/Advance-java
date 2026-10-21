package com.shopsphere.web.admin;

import com.shopsphere.model.Category;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.service.AdminService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Administration of the product catalogue: listing, creating, editing and deleting.
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {"/admin/products", "/admin/products/*"})
public class AdminProductServlet extends BaseServlet {

    private static final int PAGE_SIZE = 12;

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            if (path.endsWith("/new")) {
                request.setAttribute("categories", adminService.allCategories());
                request.setAttribute("product", new Product());
                render(request, response, "admin/product-form", "New product");
                return;
            }
            if (path.contains("/edit")) {
                long id = paramLong(request, "id", -1);
                Product product = adminService.findProduct(id).orElse(null);
                if (product == null) {
                    showError(request, response, HttpServletResponse.SC_NOT_FOUND,
                            "The product could not be found.");
                    return;
                }
                request.setAttribute("categories", adminService.allCategories());
                request.setAttribute("product", product);
                render(request, response, "admin/product-form", "Edit " + product.getName());
                return;
            }

            ProductQuery query = new ProductQuery();
            query.setSearch(paramOrNull(request, "q"));
            query.setIncludeInactive(true);
            query.setPage(paramInt(request, "page", 1));
            query.setPageSize(PAGE_SIZE);
            query.setSort(ProductQuery.Sort.fromString(request.getParameter("sort")));
            String categoryParam = paramOrNull(request, "category");
            if (categoryParam != null && categoryParam.chars().allMatch(Character::isDigit)) {
                query.setCategoryId(Long.parseLong(categoryParam));
            }

            PageResult<Product> products = adminService.listProducts(query);
            List<Category> categories = adminService.allCategories();

            request.setAttribute("products", products);
            request.setAttribute("categories", categories);
            request.setAttribute("query", query);
            request.setAttribute("sortOptions", ProductQuery.Sort.values());
            request.setAttribute("searchTerm", query.getSearch());
            render(request, response, "admin/products", "Products");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "The product list could not be loaded. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action");
        long id = paramLong(request, "id", -1);
        try {
            switch (action) {
                case "save" -> {
                    long categoryId = paramLong(request, "categoryId", 0);
                    String name = param(request, "name");
                    String sku = param(request, "sku");
                    String shortDescription = paramOrNull(request, "shortDescription");
                    String description = paramOrNull(request, "description");
                    String price = param(request, "price");
                    String comparePrice = paramOrNull(request, "comparePrice");
                    String stock = param(request, "stock");
                    String imageUrl = paramOrNull(request, "imageUrl");
                    boolean active = paramBoolean(request, "active");
                    boolean featured = paramBoolean(request, "featured");

                    AdminService.ProductInput result;
                    if (id > 0) {
                        result = adminService.updateProduct(id, name, categoryId, sku, shortDescription,
                                description, price, comparePrice, stock, imageUrl, active, featured);
                    } else {
                        result = adminService.createProduct(name, categoryId, sku, shortDescription,
                                description, price, comparePrice, stock, imageUrl, active, featured);
                    }
                    if (result.valid()) {
                        redirectWithSuccess(request, response, "/admin/products",
                                "The product was saved.");
                    } else {
                        request.setAttribute("categories", adminService.allCategories());
                        Product product = new Product();
                        product.setId(id);
                        product.setName(name);
                        product.setCategoryId(categoryId);
                        product.setSku(sku);
                        product.setShortDescription(shortDescription);
                        product.setDescription(description);
                        product.setImageUrl(imageUrl);
                        product.setActive(active);
                        product.setFeatured(featured);
                        request.setAttribute("product", product);
                        request.setAttribute("formError", result.error());
                        render(request, response, "admin/product-form",
                                id > 0 ? "Edit product" : "New product");
                    }
                }
                case "delete" -> {
                    boolean deleted = adminService.deleteProduct(id);
                    if (deleted) {
                        redirectWithSuccess(request, response, "/admin/products",
                                "The product was deleted.");
                    } else {
                        redirectWithError(request, response, "/admin/products",
                                "The product could not be deleted.");
                    }
                }
                case "toggleActive" -> {
                    Product product = adminService.findProduct(id).orElse(null);
                    if (product != null) {
                        adminService.setActive(id, !product.isActive());
                        redirectWithSuccess(request, response, "/admin/products",
                                "The product was " + (product.isActive() ? "deactivated" : "activated")
                                        + ".");
                    } else {
                        redirectWithError(request, response, "/admin/products",
                                "The product could not be found.");
                    }
                }
                case "setStock" -> {
                    int stock = paramInt(request, "stock", -1);
                    if (adminService.setStock(id, stock)) {
                        redirectWithSuccess(request, response, "/admin/products",
                                "The stock level was updated.");
                    } else {
                        redirectWithError(request, response, "/admin/products",
                                "The stock level could not be updated.");
                    }
                }
                default -> redirect(request, response, "/admin/products");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "The product could not be saved.");
        }
    }
}
