package com.shopsphere.db;

import com.shopsphere.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns the single {@link HikariDataSource} used by the whole application.
 *
 * <p>The pool is created lazily so that a failed database connection never
 * prevents the web application from starting; requests that need the database
 * fail with a clear error page instead of a stack trace.
 */
public final class Database {

    private static final Logger LOG = Logger.getLogger(Database.class.getName());

    private static volatile HikariDataSource dataSource;

    private Database() {
    }

    public static DataSource getDataSource() {
        HikariDataSource local = dataSource;
        if (local == null) {
            synchronized (Database.class) {
                local = dataSource;
                if (local == null) {
                    local = buildDataSource();
                    dataSource = local;
                }
            }
        }
        return local;
    }

    private static HikariDataSource buildDataSource() {
        AppConfig config = AppConfig.get();
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("ShopSphere-Pool");
        hikari.setJdbcUrl(config.dbUrl());
        hikari.setUsername(config.dbUser());
        hikari.setPassword(config.dbPassword());
        // The driver is loaded explicitly: inside a servlet container the JDBC
        // DriverManager may already be initialised, in which case the automatic
        // service discovery of the driver would not find the application jar.
        hikari.setDriverClassName(config.dbDriverClassName());
        hikari.setMaximumPoolSize(config.dbPoolSize());
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(config.dbConnectionTimeoutMs());
        hikari.setIdleTimeout(600_000);
        hikari.setMaxLifetime(1_800_000);
        hikari.setAutoCommit(true);
        hikari.setInitializationFailTimeout(-1);
        hikari.setConnectionTestQuery("SELECT 1");
        // Store and read timestamps in UTC; the UI renders them in the display zone.
        hikari.setConnectionInitSql("SET time_zone = '+00:00'");
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("useServerPrepStmts", "true");

        HikariDataSource ds = new HikariDataSource(hikari);
        LOG.info("HikariCP pool initialised for " + redact(config.dbUrl()) + " as " + config.dbUser());
        return ds;
    }

    /** Opens a connection from the pool. The caller owns it. */
    public static Connection connection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static boolean isAvailable() {
        try (Connection c = connection()) {
            return c.isValid(2);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Database is not reachable: " + e.getMessage());
            return false;
        }
    }

    /** Visible for tests and for a graceful JVM shutdown. */
    public static void close() {
        HikariDataSource local = dataSource;
        if (local != null) {
            local.close();
            dataSource = null;
        }
    }

    private static String redact(String jdbcUrl) {
        return jdbcUrl.replaceAll("(?i)(password|pwd)=[^&]*", "$1=***");
    }
}
