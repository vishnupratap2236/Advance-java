-- ShopSphere database provisioning
--
-- Run this script as a MySQL administrator (for example root) to create the
-- database and the least-privilege application account.
--
-- IMPORTANT: replace the password placeholders before running this file, or use
-- db\setup-database.cmd which asks for the passwords and substitutes them for you.
--
-- The application account may only touch its own schema. It needs DDL rights
-- because the application applies its own versioned schema migrations at start-up.
-- It has no rights on the mysql, information_schema, performance_schema or sys
-- schemas, and it cannot create users or grant privileges.

CREATE DATABASE IF NOT EXISTS shopsphere
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'shopsphere_app'@'localhost' IDENTIFIED BY '__APP_DB_PASSWORD__';
CREATE USER IF NOT EXISTS 'shopsphere_app'@'127.0.0.1' IDENTIFIED BY '__APP_DB_PASSWORD__';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES, DROP
    ON shopsphere.* TO 'shopsphere_app'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES, DROP
    ON shopsphere.* TO 'shopsphere_app'@'127.0.0.1';

FLUSH PRIVILEGES;

-- Verify:
--   SHOW GRANTS FOR 'shopsphere_app'@'localhost';
