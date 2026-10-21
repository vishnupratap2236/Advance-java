package com.shopsphere.it;

import com.shopsphere.model.Category;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.service.CatalogService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CatalogServiceIT {

    private final CatalogService catalogService = new CatalogService();

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    @Test
    @DisplayName("the catalogue lists products and paginates them")
    void listsAndPaginates() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setPage(1);
        query.setPageSize(4);

        PageResult<Product> page = catalogService.search(query);

        assertTrue(page.getTotalItems() >= 14);
        assertEquals(4, page.getItems().size());
        assertTrue(page.getTotalPages() >= 4);
        assertTrue(page.isHasNext());
        assertFalse(page.isHasPrevious());
    }

    @Test
    @DisplayName("a page beyond the last one falls back to the last page")
    void clampsOutOfRangePage() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setPage(999);
        query.setPageSize(9);

        PageResult<Product> page = catalogService.search(query);

        assertEquals(page.getTotalPages(), page.getPage());
        assertFalse(page.isEmpty());
    }

    @Test
    @DisplayName("the search finds products by name")
    void searchesByName() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setSearch("headphones");
        query.setPageSize(20);

        PageResult<Product> page = catalogService.search(query);

        assertTrue(page.getTotalItems() >= 1);
        assertTrue(page.getItems().stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("headphones")));
    }

    @Test
    @DisplayName("the search can be limited to one category")
    void filtersByCategory() throws SQLException {
        List<Category> categories = catalogService.activeCategories();
        Category audio = categories.stream()
                .filter(c -> "audio".equals(c.getSlug()))
                .findFirst()
                .orElseThrow();

        ProductQuery query = new ProductQuery();
        query.setCategoryId(audio.getId());
        query.setPageSize(50);

        PageResult<Product> page = catalogService.search(query);

        assertTrue(page.getTotalItems() >= 2);
        assertTrue(page.getItems().stream().allMatch(p -> p.getCategoryId() == audio.getId()));
    }

    @Test
    @DisplayName("the search can be limited by price")
    void filtersByPrice() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setMinPrice(new java.math.BigDecimal("10000"));
        query.setPageSize(50);

        PageResult<Product> page = catalogService.search(query);

        assertTrue(page.getTotalItems() >= 1);
        assertTrue(page.getItems().stream()
                .allMatch(p -> p.getPrice().compareTo(new java.math.BigDecimal("10000")) >= 0));
    }

    @Test
    @DisplayName("products can be sorted by price")
    void sortsByPrice() throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setSort(ProductQuery.Sort.PRICE_ASC);
        query.setPageSize(50);

        PageResult<Product> page = catalogService.search(query);

        double previous = -1;
        for (Product product : page.getItems()) {
            double price = product.getPrice().doubleValue();
            assertTrue(price >= previous, "The result is not sorted by price");
            previous = price;
        }
    }

    @Test
    @DisplayName("a product can be loaded by id and by slug")
    void loadsProductDetails() throws SQLException {
        Product product = catalogService.featuredProducts(1).get(0);

        assertTrue(catalogService.findProduct(product.getId()).isPresent());
        assertTrue(catalogService.findProductBySlug(product.getSlug()).isPresent());
        assertTrue(catalogService.findProduct(999_999L).isEmpty());
    }

    @Test
    @DisplayName("related products come from the same category")
    void findsRelatedProducts() throws SQLException {
        Product product = catalogService.featuredProducts(1).get(0);

        List<Product> related = catalogService.relatedProducts(product, 4);

        assertTrue(related.stream().allMatch(p -> p.getCategoryId() == product.getCategoryId()));
        assertTrue(related.stream().noneMatch(p -> p.getId() == product.getId()));
    }
}
