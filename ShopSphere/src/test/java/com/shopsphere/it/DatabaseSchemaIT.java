package com.shopsphere.it;

import com.shopsphere.db.Database;
import com.shopsphere.db.MigrationRunner;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DatabaseSchemaIT {

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    @Test
    @DisplayName("the application can open a pooled connection")
    void connectsToTheDatabase() throws SQLException {
        try (Connection connection = Database.connection()) {
            assertTrue(connection.isValid(3));
            assertTrue(connection.getMetaData() != null);
        }
    }

    @Test
    @DisplayName("running the migrations twice does not change anything")
    void migrationsAreIdempotent() throws SQLException {
        MigrationRunner.Result first = MigrationRunner.migrate();
        MigrationRunner.Result second = MigrationRunner.migrate();

        assertTrue(second.applied().isEmpty(),
                "A second run must not apply anything again, but applied: " + second.applied());
        assertTrue(second.alreadyApplied() >= 1);
    }

    @Test
    @DisplayName("all expected tables exist")
    void schemaContainsAllTables() throws SQLException {
        List<String> tables = List.of("users", "categories", "products", "product_images", "addresses",
                "carts", "cart_items", "coupons", "orders", "order_items", "order_status_history",
                "login_attempts", "schema_migrations");

        for (String table : tables) {
            try (Connection connection = Database.connection();
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT COUNT(*) FROM information_schema.tables "
                                 + "WHERE table_schema = DATABASE() AND table_name = '" + table + "'")) {
                rs.next();
                assertEquals(1, rs.getInt(1), "Missing table: " + table);
            }
        }
    }

    @Test
    @DisplayName("the demo catalogue is seeded")
    void catalogueIsSeeded() throws SQLException {
        assertTrue(TestSupport.scalar("SELECT COUNT(*) FROM categories") >= 8);
        assertTrue(TestSupport.scalar("SELECT COUNT(*) FROM products") >= 14);
        assertTrue(TestSupport.scalar("SELECT COUNT(*) FROM coupons") >= 2);
    }

    @Test
    @DisplayName("the unique constraint on the e-mail address is enforced")
    void emailMustBeUnique() throws SQLException {
        String email = TestSupport.uniqueEmail("duplicate");
        try (Connection connection = Database.connection()) {
            try {
                insertUser(connection, email);
                insertUser(connection, email);
                org.junit.jupiter.api.Assertions.fail("A duplicate e-mail address must be rejected");
            } catch (SQLException e) {
                assertTrue(e.getMessage().contains("Duplicate")
                                || e.getSQLState().startsWith("23"),
                        "Expected a constraint violation but got: " + e.getMessage());
            }
        }
    }

    private static void insertUser(Connection connection, String email) throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, 'CUSTOMER')")) {
            ps.setString(1, email);
            ps.setString(2, "$2a$12$0123456789012345678901uJ0P1cQ0Zb0Xb0C0d0E0F0G0H0I0J0K");
            ps.setString(3, "Duplicate Test");
            ps.executeUpdate();
        }
    }
}
