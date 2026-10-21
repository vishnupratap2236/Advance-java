package com.shopsphere.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Small helper for running a unit of work inside a single database transaction.
 */
public final class Transactions {

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    private Transactions() {
    }

    public static <T> T inTransaction(Work<T> work) throws SQLException {
        try (Connection connection = Database.connection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public static void inTransaction(VoidWork work) throws SQLException {
        inTransaction(connection -> {
            work.run(connection);
            return null;
        });
    }

    @FunctionalInterface
    public interface VoidWork {
        void run(Connection connection) throws SQLException;
    }
}
