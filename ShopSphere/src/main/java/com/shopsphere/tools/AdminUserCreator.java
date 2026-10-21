package com.shopsphere.tools;

import com.shopsphere.config.AppConfig;
import com.shopsphere.dao.UserDao;
import com.shopsphere.model.Role;
import com.shopsphere.model.User;
import com.shopsphere.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command line tool that creates an administrator account.
 *
 * <p>It is the documented, safe way to provision the first administrator without
 * putting a default password into the code base. The password is read from the
 * {@code SHOPSPHERE_ADMIN_PASSWORD} environment variable, from the system property
 * {@code shopsphere.admin.password} or from the first command line argument.
 *
 * <pre>
 *   java -cp target/classes com.shopsphere.tools.AdminUserCreator admin@shop.local
 *   mvn -q exec:java -Dexec.mainClass=com.shopsphere.tools.AdminUserCreator
 * </pre>
 */
public final class AdminUserCreator {

    private AdminUserCreator() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.get();
        String email = args.length > 0 ? args[0] : config.adminEmail();
        String password = args.length > 1 ? args[1] : config.adminPassword();
        String fullName = args.length > 2 ? args[2] : config.adminName();

        if (password == null || password.isBlank()) {
            System.err.println("No password supplied. Set SHOPSPHERE_ADMIN_PASSWORD, pass "
                    + "-Dshopsphere.admin.password=... or provide it as the second argument.");
            System.exit(2);
            return;
        }
        if (!com.shopsphere.util.Validation.isEmail(email)) {
            System.err.println("The email address is not valid: " + email);
            System.exit(2);
            return;
        }
        if (!com.shopsphere.util.Validation.isPasswordStrong(password)) {
            System.err.println("The password must have at least 8 characters with letters and digits.");
            System.exit(2);
            return;
        }

        try {
            UserDao userDao = new UserDao();
            Optional<User> existing = userDao.findByEmail(email.trim().toLowerCase());
            if (existing.isPresent()) {
                existing.get().setPasswordHash(PasswordHasher.hash(password));
                existing.get().setRole(Role.ADMIN);
                existing.get().setStatus("ACTIVE");
                userDao.updatePassword(existing.get().getId(), existing.get().getPasswordHash());
                System.out.println("Updated the existing account " + email + " and promoted it to admin.");
            } else {
                User admin = new User();
                admin.setEmail(email.trim().toLowerCase());
                admin.setPasswordHash(PasswordHasher.hash(password));
                admin.setFullName(fullName);
                admin.setRole(Role.ADMIN);
                admin.setStatus("ACTIVE");
                userDao.insert(admin);
                System.out.println("Created the administrator account " + email + ".");
            }
        } catch (SQLException e) {
            System.err.println("Could not create the administrator account: " + e.getMessage());
            System.exit(1);
        }
    }
}
