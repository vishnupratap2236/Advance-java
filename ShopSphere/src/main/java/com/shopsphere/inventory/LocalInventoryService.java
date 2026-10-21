package com.shopsphere.inventory;

import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.Product;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The local inventory service. It reads and writes the stock of the catalogue in the
 * database and is used whenever no remote inventory service is configured.
 */
public class LocalInventoryService implements InventoryService {

    private static final Logger LOG = Logger.getLogger(LocalInventoryService.class.getName());

    private final ProductDao productDao = new ProductDao();

    @Override
    public int getStock(long productId) {
        try {
            Optional<Product> product = productDao.findById(productId, true);
            return product.map(Product::getStock).orElse(0);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not read the stock of product " + productId, e);
            return 0;
        }
    }

    @Override
    public boolean reserve(long productId, int quantity) {
        if (quantity < 1) {
            return false;
        }
        try {
            Optional<Product> product = productDao.findById(productId, true);
            if (product.isEmpty() || product.get().getStock() < quantity) {
                return false;
            }
            return productDao.setStock(productId, product.get().getStock() - quantity);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not reserve stock of product " + productId, e);
            return false;
        }
    }

    @Override
    public void release(long productId, int quantity) {
        if (quantity < 1) {
            return;
        }
        try {
            int stock = getStock(productId);
            productDao.setStock(productId, stock + quantity);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not release the stock of product " + productId, e);
        }
    }

    @Override
    public void setStock(long productId, int stock) {
        if (stock < 0) {
            return;
        }
        try {
            productDao.setStock(productId, stock);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not set the stock of product " + productId, e);
        }
    }

    @Override
    public String describe() {
        return "Local database inventory";
    }
}
