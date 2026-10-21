package com.shopsphere.it;

import com.shopsphere.model.User;
import com.shopsphere.service.AuthService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AuthServiceIT {

    private final AuthService authService = new AuthService();

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    @Test
    @DisplayName("a customer can register and the password is stored as a hash")
    void registersCustomer() throws SQLException {
        String email = TestSupport.uniqueEmail("register");
        String password = TestSupport.randomPassword();

        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Test Customer", "+91 90000 00000");

        assertTrue(result.success(), "Registration failed: " + result.error());
        assertNotNull(result.user());
        assertEquals("CUSTOMER", result.user().getRole().name());

        String stored = TestSupport.text("SELECT password_hash FROM users WHERE email = '"
                + email + "'");
        assertNotNull(stored);
        assertFalse(stored.equals(password), "The password must never be stored in plaintext");
        assertTrue(stored.startsWith("$2"), "Expected a BCrypt hash but got: " + stored);
        assertTrue(com.shopsphere.util.PasswordHasher.matches(password, stored));

        TestSupport.deleteUser(result.user().getId());
    }

    @Test
    @DisplayName("a duplicate e-mail address is rejected")
    void rejectsDuplicateRegistration() {
        String email = TestSupport.uniqueEmail("duplicate");
        String password = TestSupport.randomPassword();

        AuthService.RegistrationResult first =
                authService.register(email, password, password, "First Customer", null);
        assertTrue(first.success());

        AuthService.RegistrationResult second =
                authService.register(email, password, password, "Second Customer", null);
        assertFalse(second.success());
        assertTrue(second.error().toLowerCase().contains("already exists"));

        TestSupport.deleteUser(first.user().getId());
    }

    @Test
    @DisplayName("invalid registrations are rejected with a message")
    void rejectsInvalidRegistration() {
        assertFalse(authService.register("not-an-email", "Passw0rd1", "Passw0rd1", "Name", null).success());
        assertFalse(authService.register("a@b.com", "short", "short", "Name", null).success());
        assertFalse(authService.register("a@b.com", "Passw0rd1", "Different1", "Name", null).success());
        assertFalse(authService.register("a@b.com", "Passw0rd1", "Passw0rd1", "", null).success());
    }

    @Test
    @DisplayName("the right password signs the user in, a wrong one does not")
    void logsInWithCorrectPassword() {
        String email = TestSupport.uniqueEmail("login");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult registration =
                authService.register(email, password, password, "Login Customer", null);
        assertTrue(registration.success());

        AuthService.LoginResult success = authService.login(email, password, "127.0.0.1");
        assertTrue(success.success(), "Login failed: " + success.error());
        assertEquals(email, success.user().getEmail());

        AuthService.LoginResult failure = authService.login(email, "WrongPassword1", "127.0.0.1");
        assertFalse(failure.success());
        assertTrue(failure.error().toLowerCase().contains("incorrect"));

        TestSupport.deleteUser(registration.user().getId());
    }

    @Test
    @DisplayName("an unknown account never reports that the e-mail does not exist")
    void unknownAccountGivesGenericError() {
        AuthService.LoginResult result =
                authService.login(TestSupport.uniqueEmail("ghost"), "Whatever123", "127.0.0.1");

        assertFalse(result.success());
        assertTrue(result.error().toLowerCase().contains("incorrect"));
    }

    @Test
    @DisplayName("repeated failures lock the account for a while")
    void locksOutAfterTooManyFailures() {
        String email = TestSupport.uniqueEmail("lockout");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult registration =
                authService.register(email, password, password, "Lockout Customer", null);
        assertTrue(registration.success());

        int maxAttempts = com.shopsphere.config.AppConfig.get().maxLoginAttempts();
        AuthService.LoginResult last = null;
        for (int i = 0; i < maxAttempts + 1; i++) {
            last = authService.login(email, "WrongPassword1", "127.0.0.1");
        }

        assertNotNull(last);
        assertTrue(last.locked(), "The account should be locked after " + maxAttempts + " failures");
        assertTrue(last.error().toLowerCase().contains("too many"));

        TestSupport.deleteUser(registration.user().getId());
    }

    @Test
    @DisplayName("the profile can be updated and the password changed")
    void updatesProfileAndPassword() throws SQLException {
        String email = TestSupport.uniqueEmail("profile");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult registration =
                authService.register(email, password, password, "Profile Customer", "+91 91111 11111");
        assertTrue(registration.success());
        long userId = registration.user().getId();

        assertTrue(authService.updateProfile(userId, "New Name", "+91 92222 22222"));
        assertEquals("New Name",
                TestSupport.text("SELECT full_name FROM users WHERE id = " + userId));

        String newPassword = TestSupport.randomPassword();
        assertTrue(authService.changePassword(userId, password, newPassword));
        assertFalse(authService.changePassword(userId, "WrongOld1", newPassword));
        assertTrue(authService.login(email, newPassword, "127.0.0.1").success());

        TestSupport.deleteUser(userId);
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}