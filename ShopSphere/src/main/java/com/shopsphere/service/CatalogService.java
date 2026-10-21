package com.shopsphere.service;

import com.shopsphere.dao.CategoryDao;
import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.Category;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Read side of the catalogue: listings, search, filters and product details.
 */
public class CatalogService {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    public PageResult<Product> search(ProductQuery query) throws SQLException {
        PageResult<Product> result = productDao.search(query);
        int totalPages = result.getTotalPages();
        if (query.getPage() > totalPages && totalPages > 0) {
            query.setPage(totalPages);
            result = productDao.search(query);
        }
        return result;
    }

    public Optional<Product> findProduct(long id) throws SQLException {
        return productDao.findById(id, false);
    }

    public Optional<Product> findProductBySlug(String slug) throws SQLException {
        return productDao.findBySlug(slug);
    }

    public List<Product> relatedProducts(Product product, int limit) throws SQLException {
        return productDao.findRelated(product, limit);
    }

    public List<Product> featuredProducts(int limit) throws SQLException {
        return productDao.findFeatured(limit);
    }

    public List<Product> latestProducts(int limit) throws SQLException {
        return productDao.findLatest(limit);
    }

    public List<Category> activeCategories() throws SQLException {
        return categoryDao.findAll(true);
    }

    public List<Category> allCategories() throws SQLException {
        return categoryDao.findAll(false);
    }

    public Optional<Category> findCategory(long id) throws SQLException {
        return categoryDao.findById(id);
    }

    public Optional<Category> findCategoryBySlug(String slug) throws SQLException {
        return categoryDao.findBySlug(slug);
    }
}
