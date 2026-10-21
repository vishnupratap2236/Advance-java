package com.shopsphere.model;

import java.math.BigDecimal;

/** A line item inside a shopping cart. */
public class CartItem {

    private long id;
    private long cartId;
    private long productId;
    private int quantity;
    private Product product;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    /** Line total is always recalculated on the server from the current product price. */
    public BigDecimal getLineTotal() {
        if (product == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isAvailable() {
        return product != null && product.isActive() && product.getStock() >= quantity;
    }

    public boolean isQuantityAboveStock() {
        return product != null && quantity > product.getStock();
    }
}
