package com.shopsphere.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Applies the versioned SQL migrations shipped inside the WAR.
 *
 * <p>Migrations live in {@code src/main/resources/db/migrations} and are listed in
 * {@code db/migrations/index.txt} so that they can be located inside a packaged
 * artifact (directory listing is not possible for resources inside a JAR/WAR).
 * Every migration is applied at most once; the applied versions are tracked in the
 * {@code schema_migrations} table.
 */
public final class MigrationRunner {

    private static final Logger LOG = Logger.getLogger(MigrationRunner.class.getName());
    private static final String MIGRATIONS_DIR = "db/migrations/";
    private static final String INDEX_RESOURCE = MIGRATIONS_DIR + "index.txt";

    private MigrationRunner() {
    }

    public record Migration(String version, String description, String fileName) {
    }

    /** Result of a migration run, useful for logging and for the admin dashboard. */
    public record Result(List<String> applied, int alreadyApplied) {
    }

    public static Result migrate() throws SQLException {
        List<Migration> migrations = loadMigrations();
        List<String> applied = new ArrayList<>();

        try (Connection connection = Database.connection()) {
            connection.setAutoCommit(true);
            ensureMigrationsTable(connection);

            for (Migration migration : migrations) {
                if (isApplied(connection, migration.version())) {
                    continue;
                }
                String sql = readResource(MIGRATIONS_DIR + migration.fileName());
                LOG.info("Applying migration " + migration.version() + " (" + migration.description() + ")");
                executeScript(connection, sql);
                recordApplied(connection, migration);
                applied.add(migration.version());
            }
        }
        return new Result(applied, migrations.size() - applied.size());
    }

    /** List of migrations that are already present in the database. */
    public static List<String> appliedVersions() throws SQLException {
        List<String> versions = new ArrayList<>();
        try (Connection connection = Database.connection()) {
            ensureMigrationsTable(connection);
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT version FROM schema_migrations ORDER BY version");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    versions.add(rs.getString(1));
                }
            }
        }
        return versions;
    }

    private static void ensureMigrationsTable(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version     VARCHAR(50)  NOT NULL,
                        description VARCHAR(255) NOT NULL,
                        applied_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (version)
                    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
                    """);
        }
    }

    private static boolean isApplied(Connection connection, String version) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM schema_migrations WHERE version = ?")) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void recordApplied(Connection connection, Migration migration) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO schema_migrations (version, description) VALUES (?, ?)")) {
            ps.setString(1, migration.version());
            ps.setString(2, migration.description());
            ps.executeUpdate();
        }
    }

    /**
     * Executes a SQL script. MySQL commits DDL implicitly, so the script is executed
     * statement by statement and a failure aborts the migration.
     */
    private static void executeScript(Connection connection, String script) throws SQLException {
        for (String statement : splitStatements(script)) {
            try (Statement st = connection.createStatement()) {
                st.execute(statement);
            } catch (SQLException e) {
                throw new SQLException("Failed executing statement: "
                        + abbreviate(statement) + " -> " + e.getMessage(), e.getSQLState(), e);
            }
        }
    }

    /**
     * Splits a SQL script into individual statements while respecting string literals,
     * quoted identifiers and comments.
     */
    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            char next = i + 1 < script.length() ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    current.append(c);
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (!inSingle && !inDouble && !inBacktick) {
                if (c == '-' && next == '-' && (i + 2 >= script.length() || isWhitespace(script.charAt(i + 2)))) {
                    inLineComment = true;
                    i++;
                    continue;
                }
                if (c == '#') {
                    inLineComment = true;
                    continue;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            if (c == '\'' && !inDouble && !inBacktick) {
                if (inSingle && next == '\'') {
                    current.append(c).append(next);
                    i++;
                    continue;
                }
                inSingle = !inSingle;
            } else if (c == '"' && !inSingle && !inBacktick) {
                inDouble = !inDouble;
            } else if (c == '`' && !inSingle && !inDouble) {
                inBacktick = !inBacktick;
            }

            if (c == ';' && !inSingle && !inDouble && !inBacktick) {
                addIfNotBlank(statements, current);
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        addIfNotBlank(statements, current);
        return statements;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static void addIfNotBlank(List<String> statements, StringBuilder current) {
        String statement = current.toString().trim();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }

    private static String abbreviate(String statement) {
        String flat = statement.replaceAll("\\s+", " ").trim();
        return flat.length() > 160 ? flat.substring(0, 160) + "..." : flat;
    }

    private static List<Migration> loadMigrations() throws SQLException {
        List<Migration> migrations = new ArrayList<>();
        try (InputStream in = MigrationRunner.class.getClassLoader().getResourceAsStream(INDEX_RESOURCE)) {
            if (in == null) {
                throw new SQLException("Migration index not found on classpath: " + INDEX_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\|", 3);
                    if (parts.length < 3) {
                        throw new SQLException("Malformed migration index line: " + trimmed);
                    }
                    String version = parts[0].trim();
                    String fileName = parts[1].trim();
                    String description = parts[2].trim();
                    migrations.add(new Migration(version, description, fileName));
                }
            }
        } catch (IOException e) {
            throw new SQLException("Could not read the migration index: " + e.getMessage(), e);
        }
        migrations.sort((a, b) -> a.version().compareTo(b.version()));
        return migrations;
    }

    private static String readResource(String resource) throws SQLException {
        try (InputStream in = MigrationRunner.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new SQLException("Migration resource not found: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Could not read migration resource: " + resource, e);
        }
    }
}
