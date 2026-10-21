package com.shopsphere.it;

import com.shopsphere.model.Category;
import com.shopsphere.model.DashboardStats;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.service.AdminService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AdminServiceIT {

    private final AdminService adminService = new AdminService();

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    @Test
    @DisplayName("the dashboard reports meaningful figures")
    void dashboardFigures() throws SQLException {
        DashboardStats stats = adminService.dashboard();

        assertTrue(stats.getTotalProducts() >= 14);
        assertTrue(stats.getTotalCustomers() >= 0);
        assertTrue(stats.getTotalOrders() >= 0);
        assertNotNull(stats.getTotalRevenue());
        assertNotNull(stats.getRecentOrders());
    }

    @Test
    @DisplayName("a product can be created, updated and deleted")
    void managesProducts() throws SQLException {
        Category category = adminService.allCategories().get(0);

        AdminService.ProductInput created = adminService.createProduct(
                "Integration Test Product", category.getId(), "SS-TEST-9001",
                "A product created by the integration test", "A longer description.",
                "1999.00", "2499.00", "12", "images/placeholder.svg", true, false);

        assertTrue(created.valid(), "Could not create the product: " + created.error());
        long productId = created.product().getId();
        try {
            assertEquals(20, created.product().getDiscountPercent());

            AdminService.ProductInput updated = adminService.updateProduct(productId,
                    "Integration Test Product", category.getId(), "SS-TEST-9001",
                    "Updated description", "A longer description.",
                    "1499.00", null, "5", "images/placeholder.svg", true, true);

            assertTrue(updated.valid(), "Could not update the product: " + updated.error());
            assertEquals(0, new java.math.BigDecimal("1499.00").compareTo(
                    adminService.findProduct(productId).get().getPrice()));
            assertTrue(adminService.findProduct(productId).get().isFeatured());
        } finally {
            assertTrue(adminService.deleteProduct(productId));
        }
        assertTrue(adminService.findProduct(productId).isEmpty());
    }

    @Test
    @DisplayName("invalid products are rejected with a message")
    void rejectsInvalidProducts() throws SQLException {
        Category category = adminService.allCategories().get(0);

        assertFalse(adminService.createProduct("", category.getId(), "SS-TEST-9002",
                null, null, "10.00", null, "1", null, true, false).valid());
        assertFalse(adminService.createProduct("No category", 0L, "SS-TEST-9003",
                null, null, "10.00", null, "1", null, true, false).valid());
        assertFalse(adminService.createProduct("Bad price", category.getId(), "SS-TEST-9004",
                null, null, "not-a-number", null, "1", null, true, false).valid());
        assertFalse(adminService.createProduct("Negative stock", category.getId(), "SS-TEST-9005",
                null, null, "10.00", null, "-4", null, true, false).valid());
        assertFalse(adminService.createProduct("Compare price", category.getId(), "SS-TEST-9006",
                null, null, "10.00", "5.00", "1", null, true, false).valid());
    }

    @Test
    @DisplayName("the stock can be corrected")
    void managesStock() throws SQLException {
        Category category = adminService.allCategories().get(0);
        AdminService.ProductInput created = adminService.createProduct(
                "Stock Test Product", category.getId(), "SS-TEST-9101",
                "Stock test", "Stock test", "999.00", null, "3", null, true, false);
        assertTrue(created.valid());
        long productId = created.product().getId();
        try {
            assertTrue(adminService.setStock(productId, 42));
            assertEquals(42, adminService.findProduct(productId).get().getStock());
            assertFalse(adminService.setStock(productId, -1));
        } finally {
            adminService.deleteProduct(productId);
        }
    }

    @Test
    @DisplayName("a category can be created, renamed and deleted")
    void managesCategories() throws SQLException {
        AdminService.CategoryInput created = adminService.createCategory(
                "Integration Test Category", "Created by a test", null, true);

        assertTrue(created.valid(), "Could not create the category: " + created.error());
        long categoryId = created.category().getId();
        try {
            AdminService.CategoryInput updated = adminService.updateCategory(categoryId,
                    "Integration Test Category Renamed", "Updated", null, true);
            assertTrue(updated.valid());
            assertEquals("Integration Test Category Renamed",
                    adminService.allCategories().stream()
                            .filter(c -> c.getId() == categoryId)
                            .findFirst().orElseThrow().getName());
        } finally {
            assertTrue(adminService.deleteCategory(categoryId));
        }
    }

    @Test
    @DisplayName("a category that still holds products cannot be deleted")
    void refusesToDeleteUsedCategory() throws SQLException {
        Category category = adminService.allCategories().stream()
                .filter(c -> {
                    try {
                        return adminService.countCategoryProducts(c.getId()) > 0;
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .findFirst()
                .orElseThrow();

        assertFalse(adminService.deleteCategory(category.getId()));
    }

    @Test
    @DisplayName("the product list can be filtered and searched")
    void listsAndSearchesProducts() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setIncludeInactive(true);
        query.setSearch("Aurora");

        var page = adminService.listProducts(query);

        assertTrue(page.getTotalItems() >= 1);
        assertTrue(page.getItems().stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("aurora")));
    }
}
