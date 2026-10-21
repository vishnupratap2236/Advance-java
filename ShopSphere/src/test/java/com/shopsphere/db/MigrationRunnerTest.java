package com.shopsphere.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationRunnerTest {

    @Test
    @DisplayName("the SQL splitter separates statements on semicolons")
    void splitsStatements() {
        List<String> statements = MigrationRunner.splitStatements(
                "CREATE TABLE a (id INT); INSERT INTO a VALUES (1);");

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE TABLE a"));
        assertTrue(statements.get(1).startsWith("INSERT INTO a"));
    }

    @Test
    @DisplayName("semicolons inside string literals do not split a statement")
    void keepsSemicolonsInsideLiterals() {
        List<String> statements = MigrationRunner.splitStatements(
                "INSERT INTO t (v) VALUES ('a;b'); SELECT 1;");

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).contains("'a;b'"));
    }

    @Test
    @DisplayName("comments are removed and do not break the split")
    void ignoresComments() {
        List<String> statements = MigrationRunner.splitStatements("""
                -- a line comment with ; inside
                CREATE TABLE b (id INT);
                /* a block comment; with semicolons */
                CREATE TABLE c (id INT);
                """);

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE TABLE b"));
        assertTrue(statements.get(1).startsWith("CREATE TABLE c"));
    }

    @Test
    @DisplayName("an empty script produces no statements")
    void handlesEmptyScript() {
        assertTrue(MigrationRunner.splitStatements("   \n  ").isEmpty());
        assertTrue(MigrationRunner.splitStatements("-- only a comment").isEmpty());
    }
}
