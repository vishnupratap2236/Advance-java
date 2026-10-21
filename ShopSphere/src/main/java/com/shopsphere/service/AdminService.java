package com.shopsphere.service;

import com.shopsphere.dao.CategoryDao;
import com.shopsphere.dao.CouponDao;
import com.shopsphere.dao.ProductDao;
import com.shopsphere.dao.UserDao;
import com.shopsphere.model.Category;
import com.shopsphere.model.Coupon;
import com.shopsphere.model.DashboardStats;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.model.User;
import com.shopsphere.util.Validation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Administration use cases: dashboard figures, catalogue management and inventory.
 */
public class AdminService {

    private static final Logger LOG = Logger.getLogger(AdminService.class.getName());
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final CouponDao couponDao = new CouponDao();
    private final UserDao userDao = new UserDao();
    private final OrderService orderService = new OrderService();

    public DashboardStats dashboard() throws SQLException {
        DashboardStats stats = new DashboardStats();
        stats.setTotalCustomers(userDao.countCustomers());
        stats.setTotalProducts(productDao.countAll());
        stats.setActiveProducts(productDao.countActive());
        stats.setTotalOrders(orderService.countAll());
        stats.setPendingOrders(orderService.countByStatus(OrderStatus.PENDING));
        BigDecimal revenue = orderService.revenue();
        stats.setTotalRevenue(revenue);
        long totalOrders = stats.getTotalOrders();
        stats.setAverageOrderValue(totalOrders == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP));
        stats.setLowStockProducts(productDao.countLowStock(LOW_STOCK_THRESHOLD));
        stats.setOutOfStockProducts(productDao.countOutOfStock());
        stats.setRecentOrders(orderService.recentOrders(6));
        stats.setTopOrders(orderService.topOrders(5));
        stats.setLowStockItems(productDao.findLowStock(LOW_STOCK_THRESHOLD, 8));
        stats.setInventoryService(
                com.shopsphere.inventory.InventoryServiceProvider.get().describe());
        return stats;
    }

    // ------------------------------------------------------------ products

    public PageResult<Product> listProducts(ProductQuery query) throws SQLException {
        return productDao.search(query);
    }

    public Optional<Product> findProduct(long id) throws SQLException {
        return productDao.findById(id, true);
    }

    /** Result of a product validation. */
    public record ProductInput(boolean valid, Product product, String error) {
    }

    public ProductInput createProduct(String name, long categoryId, String sku, String shortDescription,
                                      String description, String price, String comparePrice,
                                      String stock, String imageUrl, boolean active,
                                      boolean featured) throws SQLException {
        Product product = new Product();
        String error = validate(product, name, categoryId, sku, shortDescription, description, price,
                comparePrice, stock, imageUrl, active, featured);
        if (error != null) {
            return new ProductInput(false, null, error);
        }
        try {
            long id = productDao.insert(product);
            product.setId(id);
            return new ProductInput(true, product, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not create product", e);
            return new ProductInput(false, null, "Could not save the product. The SKU may already exist.");
        }
    }

    public ProductInput updateProduct(long id, String name, long categoryId, String sku,
                                      String shortDescription, String description, String price,
                                      String comparePrice, String stock, String imageUrl,
                                      boolean active, boolean featured) throws SQLException {
        Optional<Product> existing = findProduct(id);
        if (existing.isEmpty()) {
            return new ProductInput(false, null, "The product no longer exists.");
        }
        Product product = existing.get();
        String error = validate(product, name, categoryId, sku, shortDescription, description, price,
                comparePrice, stock, imageUrl, active, featured);
        if (error != null) {
            return new ProductInput(false, null, error);
        }
        try {
            productDao.update(product);
            return new ProductInput(true, product, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not update product", e);
            return new ProductInput(false, null, "Could not save the product. The SKU may already exist.");
        }
    }

    private String validate(Product product, String name, long categoryId, String sku,
                            String shortDescription, String description, String price,
                            String comparePrice, String stock, String imageUrl, boolean active,
                            boolean featured) throws SQLException {
        if (!Validation.isNotBlank(name) || name.trim().length() > 200) {
            return "Enter a product name.";
        }
        if (categoryId <= 0) {
            return "Choose a category.";
        }
        if (!Validation.isSku(sku)) {
            return "The SKU may only contain letters, digits, dots, dashes and underscores.";
        }
        BigDecimal parsedPrice = parseDecimal(price);
        if (parsedPrice == null || parsedPrice.signum() < 0) {
            return "Enter a valid price.";
        }
        BigDecimal parsedCompare = parseDecimal(comparePrice);
        if (comparePrice != null && !comparePrice.isBlank() && parsedCompare == null) {
            return "Enter a valid compare-at price.";
        }
        if (parsedCompare != null && parsedCompare.compareTo(parsedPrice) < 0) {
            return "The compare-at price must not be lower than the price.";
        }
        int parsedStock = Validation.parseInt(stock, -1);
        if (parsedStock < 0) {
            return "Enter a valid stock quantity.";
        }
        if (shortDescription != null && shortDescription.length() > 500) {
            return "The short description is too long.";
        }
        if (imageUrl != null && imageUrl.length() > 500) {
            return "The image path is too long.";
        }

        product.setName(name.trim());
        product.setCategoryId(categoryId);
        product.setSku(sku.trim().toUpperCase());
        product.setShortDescription(shortDescription == null || shortDescription.isBlank()
                ? null : shortDescription.trim());
        product.setDescription(description == null || description.isBlank() ? null : description.trim());
        product.setPrice(parsedPrice.setScale(2, java.math.RoundingMode.HALF_UP));
        product.setComparePrice(parsedCompare == null ? null
                : parsedCompare.setScale(2, java.math.RoundingMode.HALF_UP));
        product.setStock(parsedStock);
        product.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        product.setActive(active);
        product.setFeatured(featured);
        product.setSlug(uniqueSlug(name.trim(), product.getId()));
        return null;
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Builds a unique slug for a product name. */
    public String uniqueSlug(String name, long currentId) throws SQLException {
        String base = Validation.slugify(name);
        String slug = base;
        int suffix = 2;
        while (true) {
            Optional<Product> existing = productDao.findBySlug(slug);
            if (existing.isEmpty() || existing.get().getId() == currentId) {
                return slug;
            }
            slug = base + "-" + suffix++;
        }
    }

    public boolean deleteProduct(long id) throws SQLException {
        return productDao.delete(id);
    }

    public boolean setStock(long id, int stock) throws SQLException {
        if (stock < 0) {
            return false;
        }
        boolean updated = productDao.setStock(id, stock);
        if (updated) {
            com.shopsphere.inventory.InventoryServiceProvider.get().setStock(id, stock);
        }
        return updated;
    }

    public boolean setActive(long id, boolean active) throws SQLException {
        Optional<Product> product = productDao.findById(id, true);
        if (product.isEmpty()) {
            return false;
        }
        product.get().setActive(active);
        return productDao.update(product.get());
    }

    // ----------------------------------------------------------- categories

    /** All categories, including the hidden ones, for the administration screens. */
    public List<Category> allCategories() throws SQLException {
        return categoryDao.findAll(false);
    }

    public PageResult<Product> listCategoryProducts(long categoryId, int page, int pageSize)            throws SQLException {
        ProductQuery query = new ProductQuery();
        query.setCategoryId(categoryId);
        query.setIncludeInactive(true);
        query.setPage(page);
        query.setPageSize(pageSize);
        return productDao.search(query);
    }

    public record CategoryInput(boolean valid, Category category, String error) {
    }

    public CategoryInput createCategory(String name, String description, String imageUrl,
                                         boolean active) {
        if (!Validation.isNotBlank(name) || name.trim().length() > 120) {
            return new CategoryInput(false, null, "Enter a category name.");
        }
        if (description != null && description.length() > 500) {
            return new CategoryInput(false, null, "The description is too long.");
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setSlug(Validation.slugify(name.trim()));
        category.setDescription(description == null || description.isBlank() ? null : description.trim());
        category.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        category.setActive(active);
        try {
            long id = categoryDao.insert(category);
            category.setId(id);
            return new CategoryInput(true, category, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not create category", e);
            return new CategoryInput(false, null,
                    "Could not save the category. A category with this name already exists.");
        }
    }

    public CategoryInput updateCategory(long id, String name, String description, String imageUrl,
                                         boolean active) throws SQLException {
        Optional<Category> existing = categoryDao.findById(id);
        if (existing.isEmpty()) {
            return new CategoryInput(false, null, "The category no longer exists.");
        }
        if (!Validation.isNotBlank(name) || name.trim().length() > 120) {
            return new CategoryInput(false, null, "Enter a category name.");
        }
        Category category = existing.get();
        category.setName(name.trim());
        category.setDescription(description == null || description.isBlank() ? null : description.trim());
        category.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        category.setActive(active);
        try {
            categoryDao.update(category);
            return new CategoryInput(true, category, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not update category", e);
            return new CategoryInput(false, null,
                    "Could not save the category. A category with this name already exists.");
        }
    }

    public boolean deleteCategory(long id) throws SQLException {
        return categoryDao.delete(id);
    }

    public long countCategoryProducts(long id) throws SQLException {
        return categoryDao.countProducts(id);
    }

    // -------------------------------------------------------------- coupons

    public List<Coupon> listCoupons() throws SQLException {
        return couponDao.findAll();
    }

    public boolean deleteCoupon(long id) throws SQLException {
        return couponDao.delete(id);
    }

    // ------------------------------------------------------------ customers

    public PageResult<User> listCustomers(int page, int pageSize) throws SQLException {
        return userDao.findCustomers(page, pageSize);
    }

    public boolean setCustomerStatus(long userId, String status) throws SQLException {
        if (!"ACTIVE".equals(status) && !"SUSPENDED".equals(status)) {
            return false;
        }
        return userDao.updateStatus(userId, status);
    }
}

