package com.shopsphere.service;

import com.shopsphere.dao.WishlistDao;
import com.shopsphere.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * The wishlist of a customer.
 */
public class WishlistService {

    private final WishlistDao wishlistDao = new WishlistDao();

    public List<Product> list(long userId) throws SQLException {
        return wishlistDao.findProducts(userId);
    }

    public boolean contains(long userId, long productId) throws SQLException {
        return wishlistDao.contains(userId, productId);
    }

    /**
     * Adds the product to the wishlist or removes it when it is already saved.
     *
     * @return {@code true} when the product is on the wishlist afterwards
     */
    public boolean toggle(long userId, long productId) throws SQLException {
        if (wishlistDao.contains(userId, productId)) {
            wishlistDao.remove(userId, productId);
            return false;
        }
        wishlistDao.add(userId, productId);
        return true;
    }

    public void remove(long userId, long productId) throws SQLException {
        wishlistDao.remove(userId, productId);
    }
}
