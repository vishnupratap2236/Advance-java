package com.shopsphere.it;

import com.shopsphere.dao.PaymentDao;
import com.shopsphere.dao.ProductDao;
import com.shopsphere.model.Payment;
import com.shopsphere.service.AuthService;
import com.shopsphere.service.CartService;
import com.shopsphere.service.OrderService;
import com.shopsphere.test.TestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PaymentAndCouponIT {

    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();
    private final ProductDao productDao = new ProductDao();
    private final PaymentDao paymentDao = new PaymentDao();

    private long userId;

    @BeforeAll
    static void checkDatabase() {
        assumeTrue(TestSupport.databaseAvailable(),
                "The ShopSphere database is not reachable - skipping the integration tests.");
    }

    private void createUser() {
        String email = TestSupport.uniqueEmail("payment");
        String password = TestSupport.randomPassword();
        AuthService.RegistrationResult result =
                authService.register(email, password, password, "Payment Customer", null);
        assertTrue(result.success());
        userId = result.user().getId();
    }

    @Test
    @DisplayName("a payment record is written for every order")
    void createsPaymentRecord() throws Exception {
        createUser();
        try {
            var product = productDao.findFeatured(50).stream()
                    .filter(p -> p.getStock() > 5)
                    .findFirst()
                    .orElseThrow();
            cartService.addItem(userId, product.getId(), 1);

            var order = orderService.placeOrder(new OrderService.CheckoutRequest(userId,
                    "Test Recipient", "+91 90000 00000", "12 MG Road", null, "Bengaluru",
                    "Karnataka", "560001", "India", null, "COD", null));

            Optional<Payment> payment = paymentDao.findByOrderId(order.getId());
            assertTrue(payment.isPresent(), "No payment record was created");
            assertEquals(order.getTotal(), payment.get().getAmount());
            assertEquals("COD", payment.get().getPaymentMethod());
            assertEquals("PENDING", payment.get().getPaymentStatus());
            assertNotNull(payment.get().getGatewayResponse());
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @Test
    @DisplayName("a coupon can only be used once per customer")
    void enforcesPerCustomerCouponLimit() throws Exception {
        createUser();
        try {
            var product = productDao.findFeatured(50).stream()
                    .filter(p -> p.getStock() > 5)
                    .findFirst()
                    .orElseThrow();

            cartService.addItem(userId, product.getId(), 2);
            var first = orderService.placeOrder(new OrderService.CheckoutRequest(userId,
                    "Test Recipient", "+91 90000 00000", "12 MG Road", null, "Bengaluru",
                    "Karnataka", "560001", "India", null, "COD", "WELCOME10"));
            assertTrue(first.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0);

            // The customer already used the coupon, so the second order is rejected.
            cartService.addItem(userId, product.getId(), 2);
            assertThrows(OrderService.CheckoutException.class,
                    () -> orderService.placeOrder(new OrderService.CheckoutRequest(userId,
                            "Test Recipient", "+91 90000 00000", "12 MG Road", null, "Bengaluru",
                            "Karnataka", "560001", "India", null, "COD", "WELCOME10")));

            // Without the coupon the order is placed normally.
            var third = orderService.placeOrder(new OrderService.CheckoutRequest(userId,
                    "Test Recipient", "+91 90000 00000", "12 MG Road", null, "Bengaluru",
                    "Karnataka", "560001", "India", null, "COD", null));
            assertEquals(0, third.getDiscount().compareTo(java.math.BigDecimal.ZERO));
        } finally {
            TestSupport.deleteUser(userId);
        }
    }

    @AfterAll
    static void cleanUp() {
        TestSupport.deleteAllTestUsers();
    }
}