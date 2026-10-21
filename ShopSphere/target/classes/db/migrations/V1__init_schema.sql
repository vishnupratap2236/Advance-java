-- ShopSphere :: V1 - Initial schema
-- Author: ShopSphere Engineering
-- Engine: MySQL 8.4+ / 26.x (InnoDB, utf8mb4)

CREATE TABLE IF NOT EXISTS schema_migrations (
    version     VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    applied_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (version)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------- users
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email         VARCHAR(190)    NOT NULL,
    password_hash VARCHAR(100)    NOT NULL,
    full_name     VARCHAR(120)    NOT NULL,
    phone         VARCHAR(40)     NULL,
    role          VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    status        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role_status (role, status),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------ categories
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120)    NOT NULL,
    slug        VARCHAR(140)    NOT NULL,
    description VARCHAR(500)    NULL,
    image_url   VARCHAR(500)    NULL,
    active      TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    UNIQUE KEY uk_categories_slug (slug),
    KEY idx_categories_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -------------------------------------------------------------- products
CREATE TABLE IF NOT EXISTS products (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id        BIGINT UNSIGNED NOT NULL,
    name               VARCHAR(200)    NOT NULL,
    slug               VARCHAR(220)    NOT NULL,
    sku                VARCHAR(60)     NOT NULL,
    short_description  VARCHAR(500)    NULL,
    description        TEXT            NULL,
    price              DECIMAL(10, 2)  NOT NULL,
    compare_price      DECIMAL(10, 2)  NULL,
    stock              INT             NOT NULL DEFAULT 0,
    image_url          VARCHAR(500)    NULL,
    active             TINYINT(1)      NOT NULL DEFAULT 1,
    featured           TINYINT(1)      NOT NULL DEFAULT 0,
    rating             DECIMAL(3, 2)   NOT NULL DEFAULT 0.00,
    review_count       INT             NOT NULL DEFAULT 0,
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_products_slug (slug),
    UNIQUE KEY uk_products_sku (sku),
    KEY idx_products_category (category_id),
    KEY idx_products_browse (active, featured, id),
    KEY idx_products_price (price),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_compare_price CHECK (compare_price IS NULL OR compare_price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_rating CHECK (rating >= 0 AND rating <= 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS product_images (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id BIGINT UNSIGNED NOT NULL,
    image_url  VARCHAR(500)    NOT NULL,
    alt_text   VARCHAR(200)    NULL,
    sort_order INT             NOT NULL DEFAULT 0,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_images_product (product_id, sort_order),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- -------------------------------------------------------------- addresses
CREATE TABLE IF NOT EXISTS addresses (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id        BIGINT UNSIGNED NOT NULL,
    recipient_name VARCHAR(120)    NOT NULL,
    phone          VARCHAR(40)     NOT NULL,
    line1          VARCHAR(200)    NOT NULL,
    line2          VARCHAR(200)    NULL,
    city           VARCHAR(100)    NOT NULL,
    state          VARCHAR(100)    NOT NULL,
    postal_code    VARCHAR(20)     NOT NULL,
    country        VARCHAR(100)    NOT NULL DEFAULT 'India',
    is_default     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_addresses_user (user_id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------------------------------------------- carts
CREATE TABLE IF NOT EXISTS carts (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_user (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    cart_id    BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity   INT             NOT NULL DEFAULT 1,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_items_cart_product (cart_id, product_id),
    KEY idx_cart_items_product (product_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- --------------------------------------------------------------- coupons
CREATE TABLE IF NOT EXISTS coupons (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code               VARCHAR(40)     NOT NULL,
    discount_percent   DECIMAL(5, 2)   NOT NULL,
    min_order_amount   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    max_discount       DECIMAL(10, 2)  NULL,
    usage_limit        INT             NULL,
    times_used         INT             NOT NULL DEFAULT 0,
    starts_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at         TIMESTAMP       NULL,
    active             TINYINT(1)      NOT NULL DEFAULT 1,
    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupons_code (code),
    KEY idx_coupons_active (active),
    CONSTRAINT ck_coupons_percent CHECK (discount_percent > 0 AND discount_percent <= 100),
    CONSTRAINT ck_coupons_min_amount CHECK (min_order_amount >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------- orders
CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_number   VARCHAR(32)     NOT NULL,
    user_id        BIGINT UNSIGNED NOT NULL,
    status         VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    subtotal       DECIMAL(12, 2)  NOT NULL,
    discount       DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    shipping_fee   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    tax            DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    total          DECIMAL(12, 2)  NOT NULL,
    coupon_code    VARCHAR(40)     NULL,
    payment_method VARCHAR(30)     NOT NULL DEFAULT 'COD',
    payment_status VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    recipient_name VARCHAR(120)    NOT NULL,
    phone          VARCHAR(40)     NOT NULL,
    address_line1  VARCHAR(200)    NOT NULL,
    address_line2  VARCHAR(200)    NULL,
    city           VARCHAR(100)    NOT NULL,
    state          VARCHAR(100)    NOT NULL,
    postal_code    VARCHAR(20)     NOT NULL,
    country        VARCHAR(100)    NOT NULL DEFAULT 'India',
    notes          VARCHAR(500)    NULL,
    placed_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_number (order_number),
    KEY idx_orders_user_placed (user_id, placed_at),
    KEY idx_orders_status_placed (status, placed_at),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_orders_status CHECK (status IN
        ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_orders_payment_status CHECK (payment_status IN
        ('PENDING', 'PAID', 'REFUNDED', 'FAILED')),
    CONSTRAINT ck_orders_payment_method CHECK (payment_method IN ('COD', 'DEMO_CARD', 'BANK_TRANSFER'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id     BIGINT UNSIGNED NOT NULL,
    product_id   BIGINT UNSIGNED NULL,
    product_name VARCHAR(200)    NOT NULL,
    product_sku  VARCHAR(60)     NULL,
    image_url    VARCHAR(500)    NULL,
    unit_price   DECIMAL(10, 2)  NOT NULL,
    quantity     INT             NOT NULL,
    line_total   DECIMAL(12, 2)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_items_order (order_id),
    KEY idx_order_items_product (product_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_status_history (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id   BIGINT UNSIGNED NOT NULL,
    status     VARCHAR(30)     NOT NULL,
    note       VARCHAR(255)    NULL,
    changed_by BIGINT UNSIGNED NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_history_order (order_id, created_at),
    CONSTRAINT fk_order_history_order FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_history_user FOREIGN KEY (changed_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------- login attempts
CREATE TABLE IF NOT EXISTS login_attempts (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email        VARCHAR(190)    NOT NULL,
    ip_address   VARCHAR(64)     NULL,
    success      TINYINT(1)      NOT NULL DEFAULT 0,
    attempted_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_login_attempts_email_time (email, attempted_at),
    KEY idx_login_attempts_time (attempted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
