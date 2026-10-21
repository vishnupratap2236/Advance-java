package com.shopsphere.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Central application configuration.
 *
 * <p>Resolution order for every setting (first hit wins):
 * <ol>
 *   <li>JVM system property, e.g. {@code -Dshopsphere.db.url=...}</li>
 *   <li>Environment variable, e.g. {@code DB_URL}</li>
 *   <li>External properties file pointed at by {@code SHOPSPHERE_CONFIG} or
 *       {@code -Dshopsphere.config=/path/to/db.properties}</li>
 *   <li>Built-in default</li>
 * </ol>
 *
 * <p>No secret is ever stored in the source tree. The file referenced by
 * {@code SHOPSPHERE_CONFIG} is expected to live outside the repository (or to be
 * git-ignored); {@code config/db.example.properties} documents the format.
 */
public final class AppConfig {

    private static final Logger LOG = Logger.getLogger(AppConfig.class.getName());

    private static volatile AppConfig instance;

    private final Properties properties = new Properties();
    private final String sourceDescription;

    private AppConfig() {
        String filePath = firstNonBlank(
                System.getProperty("shopsphere.config"),
                System.getenv("SHOPSPHERE_CONFIG"));

        StringBuilder source = new StringBuilder("built-in defaults");
        if (filePath != null) {
            Path path = Path.of(filePath);
            if (Files.isReadable(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    properties.load(in);
                    source = new StringBuilder("file " + path.toAbsolutePath());
                } catch (IOException e) {
                    LOG.warning("Could not read config file " + path + ": " + e.getMessage());
                }
            } else {
                LOG.warning("Config file not readable: " + path);
            }
        }
        this.sourceDescription = source.toString();
    }

    public static AppConfig get() {
        AppConfig local = instance;
        if (local == null) {
            synchronized (AppConfig.class) {
                local = instance;
                if (local == null) {
                    local = new AppConfig();
                    instance = local;
                }
            }
        }
        return local;
    }

    /** Visible for testing: drops the cached configuration. */
    public static void reload() {
        synchronized (AppConfig.class) {
            instance = null;
        }
    }

    // ------------------------------------------------------------- database

    public String dbUrl() {
        return value("shopsphere.db.url", "DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/shopsphere?useUnicode=true&characterEncoding=UTF-8"
                        + "&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
                        + "&rewriteBatchedStatements=true");
    }

    public String dbUser() {
        return value("shopsphere.db.user", "DB_USER", "db.user", "shopsphere_app");
    }

    public String dbPassword() {
        return value("shopsphere.db.password", "DB_PASSWORD", "db.password", "");
    }

    /** The JDBC driver class, loaded explicitly inside the servlet container. */
    public String dbDriverClassName() {
        return value("shopsphere.db.driver", "DB_DRIVER", "db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public int dbPoolSize() {
        return intValue("shopsphere.db.poolSize", "DB_POOL_SIZE", "db.poolSize", 12);
    }

    public int dbConnectionTimeoutMs() {
        return intValue("shopsphere.db.connectionTimeoutMs", "DB_CONNECTION_TIMEOUT_MS",
                "db.connectionTimeoutMs", 20_000);
    }

    public boolean dbMigrateOnStartup() {
        return booleanValue("shopsphere.db.migrateOnStartup", "DB_MIGRATE_ON_STARTUP",
                "db.migrateOnStartup", true);
    }

    // -------------------------------------------------------------- app

    /** Email of the administrator created on first start when none exists yet. */
    public String adminEmail() {
        return value("shopsphere.admin.email", "SHOPSPHERE_ADMIN_EMAIL", "admin.email",
                "admin@shopsphere.local");
    }

    /**
     * Password of the bootstrap administrator. When blank and no administrator exists,
     * no account is created and a warning is logged instead.
     */
    public String adminPassword() {
        return value("shopsphere.admin.password", "SHOPSPHERE_ADMIN_PASSWORD", "admin.password", "");
    }

    public String adminName() {
        return value("shopsphere.admin.name", "SHOPSPHERE_ADMIN_NAME", "admin.name", "Store Administrator");
    }

    public String appName() {
        return value("shopsphere.app.name", "SHOPSPHERE_APP_NAME", "app.name", "ShopSphere");
    }

    public String currencySymbol() {
        return value("shopsphere.currency", "SHOPSPHERE_CURRENCY", "currency", "₹");
    }

    /** Zone used to render timestamps that are stored in UTC. */
    public java.time.ZoneId displayZone() {
        String raw = value("shopsphere.timezone", "SHOPSPHERE_TIMEZONE", "timezone", "");
        if (raw == null || raw.isBlank()) {
            return java.time.ZoneId.systemDefault();
        }
        try {
            return java.time.ZoneId.of(raw.trim());
        } catch (RuntimeException e) {
            LOG.warning("Invalid timezone '" + raw + "', falling back to system default");
            return java.time.ZoneId.systemDefault();
        }
    }

    public String configSource() {
        return sourceDescription;
    }

    // ------------------------------------------------------- commerce rules

    public java.math.BigDecimal shippingFee() {
        return decimalValue("shopsphere.shipping.fee", "SHIPPING_FEE", "shipping.fee", "99.00");
    }

    public java.math.BigDecimal freeShippingThreshold() {
        return decimalValue("shopsphere.shipping.freeThreshold", "SHIPPING_FREE_THRESHOLD",
                "shipping.freeThreshold", "4999.00");
    }

    public java.math.BigDecimal taxRate() {
        return decimalValue("shopsphere.tax.rate", "TAX_RATE", "tax.rate", "0.05");
    }

    public int maxLoginAttempts() {
        return intValue("shopsphere.security.maxLoginAttempts", "MAX_LOGIN_ATTEMPTS",
                "security.maxLoginAttempts", 5);
    }

    public int loginLockoutMinutes() {
        return intValue("shopsphere.security.loginLockoutMinutes", "LOGIN_LOCKOUT_MINUTES",
                "security.loginLockoutMinutes", 15);
    }

    // ---------------------------------------------------------- inventory

    /** Which inventory transport is used: local, socket or rmi. */
    public String inventoryMode() {
        return value("shopsphere.inventory.mode", "SHOPSPHERE_INVENTORY_MODE", "inventory.mode", "local");
    }

    public String inventoryHost() {
        return value("shopsphere.inventory.host", "SHOPSPHERE_INVENTORY_HOST", "inventory.host",
                "127.0.0.1");
    }

    public int inventoryPort() {
        return intValue("shopsphere.inventory.port", "SHOPSPHERE_INVENTORY_PORT", "inventory.port",
                9099);
    }

    public int inventoryTimeoutMs() {
        return intValue("shopsphere.inventory.timeoutMs", "SHOPSPHERE_INVENTORY_TIMEOUT_MS",
                "inventory.timeoutMs", 1500);
    }

    public String inventoryServiceName() {
        return value("shopsphere.inventory.serviceName", "SHOPSPHERE_INVENTORY_SERVICE_NAME",
                "inventory.serviceName", "ShopSphereInventory");
    }

    // ------------------------------------------------------------ internals

    private String value(String systemProperty, String envVariable, String fileKey, String fallback) {
        String v = System.getProperty(systemProperty);
        if (isBlank(v)) {
            v = System.getenv(envVariable);
        }
        if (isBlank(v)) {
            v = properties.getProperty(fileKey);
        }
        return isBlank(v) ? fallback : v.trim();
    }

    private int intValue(String systemProperty, String envVariable, String fileKey, int fallback) {
        String raw = value(systemProperty, envVariable, fileKey, null);
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            LOG.warning("Invalid integer for " + systemProperty + ": " + raw);
            return fallback;
        }
    }

    private boolean booleanValue(String systemProperty, String envVariable, String fileKey, boolean fallback) {
        String raw = value(systemProperty, envVariable, fileKey, null);
        if (raw == null) {
            return fallback;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    private java.math.BigDecimal decimalValue(String systemProperty, String envVariable,
                                              String fileKey, String fallback) {
        String raw = value(systemProperty, envVariable, fileKey, fallback);
        try {
            return new java.math.BigDecimal(raw.trim());
        } catch (RuntimeException e) {
            LOG.warning("Invalid decimal for " + systemProperty + ": " + raw);
            return java.math.BigDecimal.ZERO;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }
}
