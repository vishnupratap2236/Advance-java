-- ShopSphere :: V3 - Payments, coupon usage tracking, reviews and wishlist
-- These tables mirror the entities of the original reference implementation.

CREATE TABLE IF NOT EXISTS payments (
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id               BIGINT UNSIGNED NOT NULL,
    payment_method         VARCHAR(30)     NOT NULL,
    transaction_id         VARCHAR(64)     NULL,
    amount                 DECIMAL(12, 2)  NOT NULL,
    payment_status         VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    payment_gateway_response VARCHAR(500)  NULL,
    paid_at                TIMESTAMP       NULL,
    created_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_payments_order (order_id),
    KEY idx_payments_status (payment_status),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_payments_amount CHECK (amount >= 0),
    CONSTRAINT ck_payments_status CHECK (payment_status IN
        ('PENDING', 'PAID', 'REFUNDED', 'FAILED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS coupon_usage (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    coupon_id        BIGINT UNSIGNED NOT NULL,
    user_id          BIGINT UNSIGNED NOT NULL,
    order_id         BIGINT UNSIGNED NULL,
    discount_applied DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    used_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_coupon_usage_coupon_user (coupon_id, user_id),
    KEY idx_coupon_usage_order (order_id),
    CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_coupon_usage_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_coupon_usage_order FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

ALTER TABLE coupons
    ADD COLUMN usage_per_user INT NOT NULL DEFAULT 1 AFTER usage_limit;

CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id BIGINT UNSIGNED NOT NULL,
    user_id    BIGINT UNSIGNED NOT NULL,
    rating     TINYINT         NOT NULL,
    title      VARCHAR(150)    NULL,
    comment    TEXT            NULL,
    status     VARCHAR(20)     NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_product_user (product_id, user_id),
    KEY idx_reviews_product_status (product_id, status),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT ck_reviews_status CHECK (status IN ('PUBLISHED', 'HIDDEN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS wishlist_items (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlist_user_product (user_id, product_id),
    KEY idx_wishlist_product (product_id),
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
