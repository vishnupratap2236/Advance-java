package com.shopsphere.web.admin;

import com.shopsphere.model.Category;
import com.shopsphere.service.AdminService;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Administration of the product categories.
 */
@WebServlet(name = "AdminCategoryServlet", urlPatterns = {"/admin/categories"})
public class AdminCategoryServlet extends BaseServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Category> categories = adminService.allCategories();
            request.setAttribute("categories", categories);
            render(request, response, "admin/categories", "Categories");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "The categories could not be loaded. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action");
        long id = paramLong(request, "id", -1);
        try {
            switch (action) {
                case "create" -> {
                    AdminService.CategoryInput result = adminService.createCategory(
                            param(request, "name"), paramOrNull(request, "description"),
                            paramOrNull(request, "imageUrl"), paramBoolean(request, "active"));
                    if (result.valid()) {
                        redirectWithSuccess(request, response, "/admin/categories",
                                "The category was created.");
                    } else {
                        request.setAttribute("categories", adminService.allCategories());
                        request.setAttribute("formError", result.error());
                        render(request, response, "admin/categories", "Categories");
                    }
                }
                case "update" -> {
                    AdminService.CategoryInput result = adminService.updateCategory(id,
                            param(request, "name"), paramOrNull(request, "description"),
                            paramOrNull(request, "imageUrl"), paramBoolean(request, "active"));
                    if (result.valid()) {
                        redirectWithSuccess(request, response, "/admin/categories",
                                "The category was updated.");
                    } else {
                        request.setAttribute("categories", adminService.allCategories());
                        request.setAttribute("formError", result.error());
                        render(request, response, "admin/categories", "Categories");
                    }
                }
                case "delete" -> {
                    long productCount = adminService.countCategoryProducts(id);
                    if (productCount > 0) {
                        redirectWithError(request, response, "/admin/categories",
                                "This category still holds " + productCount
                                        + " product(s) and cannot be deleted. "
                                        + "Move or delete the products first.");
                    } else if (adminService.deleteCategory(id)) {
                        redirectWithSuccess(request, response, "/admin/categories",
                                "The category was deleted.");
                    } else {
                        redirectWithError(request, response, "/admin/categories",
                                "The category could not be deleted.");
                    }
                }
                default -> redirect(request, response, "/admin/categories");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "The category could not be saved.");
        }
    }
}
