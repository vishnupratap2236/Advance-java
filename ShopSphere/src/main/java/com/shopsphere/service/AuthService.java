package com.shopsphere.service;

import com.shopsphere.config.AppConfig;
import com.shopsphere.dao.UserDao;
import com.shopsphere.model.Role;
import com.shopsphere.model.User;
import com.shopsphere.util.PasswordHasher;
import com.shopsphere.util.Validation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Registration, authentication and account management.
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final UserDao userDao = new UserDao();

    /** Outcome of a registration attempt. */
    public record RegistrationResult(boolean success, User user, String error) {
    }

    /** Outcome of a login attempt. */
    public record LoginResult(boolean success, User user, String error, boolean locked) {
    }

    public RegistrationResult register(String email, String password, String confirmPassword,
                                       String fullName, String phone) {
        String normalisedEmail = email == null ? "" : email.trim().toLowerCase();
        if (!Validation.isEmail(normalisedEmail)) {
            return new RegistrationResult(false, null, "Enter a valid email address.");
        }
        if (!Validation.isPasswordStrong(password)) {
            return new RegistrationResult(false, null,
                    "Password must be at least 8 characters and contain letters and digits.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            return new RegistrationResult(false, null, "The two passwords do not match.");
        }
        if (!Validation.isNotBlank(fullName) || fullName.trim().length() > 120) {
            return new RegistrationResult(false, null, "Enter your full name.");
        }
        if (phone != null && !phone.isBlank() && !Validation.isPhone(phone)) {
            return new RegistrationResult(false, null, "Enter a valid phone number.");
        }
        try {
            if (userDao.existsByEmail(normalisedEmail)) {
                return new RegistrationResult(false, null,
                        "An account with this email address already exists.");
            }
            User user = new User();
            user.setEmail(normalisedEmail);
            user.setPasswordHash(PasswordHasher.hash(password));
            user.setFullName(fullName.trim());
            user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
            user.setRole(Role.CUSTOMER);
            user.setStatus("ACTIVE");
            long id = userDao.insert(user);
            user.setId(id);
            return new RegistrationResult(true, user, null);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Registration failed", e);
            return new RegistrationResult(false, null,
                    "We could not create the account right now. Please try again later.");
        }
    }

    public LoginResult login(String email, String password, String ipAddress) {
        String normalisedEmail = email == null ? "" : email.trim().toLowerCase();
        try {
            int maxAttempts = AppConfig.get().maxLoginAttempts();
            int lockoutMinutes = AppConfig.get().loginLockoutMinutes();
            LocalDateTime windowStart = LocalDateTime.now().minusMinutes(lockoutMinutes);
            int failures = userDao.countRecentFailures(normalisedEmail, windowStart);
            if (failures >= maxAttempts) {
                userDao.recordLoginAttempt(normalisedEmail, ipAddress, false);
                return new LoginResult(false, null,
                        "Too many failed attempts. Please try again in " + lockoutMinutes + " minutes.",
                        true);
            }

            Optional<User> found = userDao.findByEmail(normalisedEmail);
            if (found.isEmpty() || !PasswordHasher.matches(password, found.get().getPasswordHash())) {
                userDao.recordLoginAttempt(normalisedEmail, ipAddress, false);
                return new LoginResult(false, null, "Incorrect email or password.", false);
            }
            User user = found.get();
            if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
                userDao.recordLoginAttempt(normalisedEmail, ipAddress, false);
                return new LoginResult(false, null,
                        "This account has been suspended. Please contact support.", false);
            }
            if (PasswordHasher.needsRehash(user.getPasswordHash())) {
                user.setPasswordHash(PasswordHasher.hash(password));
                userDao.updatePassword(user.getId(), user.getPasswordHash());
            }
            userDao.recordLoginAttempt(normalisedEmail, ipAddress, true);
            userDao.clearLoginAttempts(normalisedEmail);
            return new LoginResult(true, user, null, false);
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Login failed", e);
            return new LoginResult(false, null,
                    "We could not sign you in right now. Please try again later.", false);
        }
    }

    public boolean updateProfile(long userId, String fullName, String phone) {
        if (!Validation.isNotBlank(fullName) || fullName.trim().length() > 120) {
            return false;
        }
        if (phone != null && !phone.isBlank() && !Validation.isPhone(phone)) {
            return false;
        }
        try {
            return userDao.updateProfile(userId, fullName.trim(),
                    phone == null || phone.isBlank() ? null : phone.trim());
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Profile update failed", e);
            return false;
        }
    }

    public boolean changePassword(long userId, String currentPassword, String newPassword) {
        try {
            Optional<User> found = userDao.findById(userId);
            if (found.isEmpty()) {
                return false;
            }
            if (!PasswordHasher.matches(currentPassword, found.get().getPasswordHash())) {
                return false;
            }
            if (!Validation.isPasswordStrong(newPassword)) {
                return false;
            }
            return userDao.updatePassword(userId, PasswordHasher.hash(newPassword));
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Password change failed", e);
            return false;
        }
    }

    /**
     * Creates the initial administrator when no administrator exists yet.
     *
     * <p>The password is taken from the configuration; when it is empty no account is
     * created and the administrator has to be provisioned with the documented CLI tool.
     *
     * @return the created administrator or {@code null} when nothing was created
     */
    public User bootstrapAdmin() {
        AppConfig config = AppConfig.get();
        String password = config.adminPassword();
        if (password == null || password.isBlank()) {
            try {
                if (userDao.countByRole(Role.ADMIN) == 0) {
                    LOG.warning("No administrator account exists and no bootstrap password is "
                            + "configured. Use the AdminUserCreator tool or set "
                            + "SHOPSPHERE_ADMIN_PASSWORD to create one.");
                }
            } catch (SQLException e) {
                LOG.log(java.util.logging.Level.SEVERE, "Could not verify administrator accounts", e);
            }
            return null;
        }
        try {
            if (userDao.countByRole(Role.ADMIN) > 0) {
                return null;
            }
            String email = config.adminEmail().trim().toLowerCase();
            if (userDao.existsByEmail(email)) {
                return null;
            }
            User admin = new User();
            admin.setEmail(email);
            admin.setPasswordHash(PasswordHasher.hash(password));
            admin.setFullName(config.adminName());
            admin.setRole(Role.ADMIN);
            admin.setStatus("ACTIVE");
            long id = userDao.insert(admin);
            admin.setId(id);
            LOG.info("Bootstrap administrator '" + email + "' created");
            return admin;
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Could not create the bootstrap administrator", e);
            return null;
        }
    }
}
