package com.shopsphere.util;

import com.shopsphere.model.Coupon;
import com.shopsphere.model.PageResult;
import com.shopsphere.model.Product;
import com.shopsphere.model.ProductQuery;
import com.shopsphere.model.OrderStatus;
import com.shopsphere.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelBehaviourTest {

    @Test
    @DisplayName("a coupon caps the discount and never exceeds the subtotal")
    void couponDiscountIsCapped() {
        Coupon coupon = new Coupon();
        coupon.setDiscountPercent(new BigDecimal("10"));
        coupon.setMaxDiscount(new BigDecimal("50"));

        assertEquals(0, new BigDecimal("50.00").compareTo(coupon.discountFor(new BigDecimal("1000"))));
        assertEquals(0, new BigDecimal("5.00").compareTo(coupon.discountFor(new BigDecimal("50"))));
        assertEquals(0, BigDecimal.ZERO.compareTo(coupon.discountFor(BigDecimal.ZERO)));
    }

    @Test
    @DisplayName("a product reports its discount and stock state")
    void productDerivedState() {
        Product product = new Product();
        product.setPrice(new BigDecimal("800"));
        product.setComparePrice(new BigDecimal("1000"));

        assertTrue(product.isOnSale());
        assertEquals(20, product.getDiscountPercent());
        assertTrue(product.isOutOfStock());

        product.setStock(3);
        assertTrue(product.isLowStock());
        assertTrue(product.isInStock());
    }

    @Test
    @DisplayName("pagination metadata is calculated correctly")
    void pageResultMetadata() {
        PageResult<String> page = new PageResult<>(List.of("a", "b", "c"), 2, 3, 7);

        assertEquals(3, page.getTotalPages());
        assertTrue(page.isHasPrevious());
        assertTrue(page.isHasNext());
        assertEquals(1, page.getPreviousPage());
        assertEquals(3, page.getNextPage());
        assertEquals(4, page.getFirstItemIndex());
        assertEquals(6, page.getLastItemIndex());
    }

    @Test
    @DisplayName("an empty page is handled without errors")
    void emptyPage() {
        PageResult<String> page = new PageResult<>(List.of(), 1, 10, 0);

        assertTrue(page.isEmpty());
        assertEquals(0, page.getFirstItemIndex());
        assertEquals(1, page.getTotalPages());
    }

    @Test
    @DisplayName("unknown sort values fall back to the default")
    void sortFallsBackToDefault() {
        assertEquals(ProductQuery.Sort.NEWEST, ProductQuery.Sort.fromString("nonsense"));
        assertEquals(ProductQuery.Sort.PRICE_ASC, ProductQuery.Sort.fromString("price_asc"));
    }

    @Test
    @DisplayName("roles and statuses are parsed defensively")
    void enumParsing() {
        assertEquals(Role.ADMIN, Role.fromString("admin"));
        assertEquals(Role.CUSTOMER, Role.fromString("nonsense"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.fromString("delivered"));
        assertEquals(OrderStatus.PENDING, OrderStatus.fromString(null));
        assertTrue(OrderStatus.PENDING.isCancellable());
        assertFalse(OrderStatus.DELIVERED.isCancellable());
    }
}
