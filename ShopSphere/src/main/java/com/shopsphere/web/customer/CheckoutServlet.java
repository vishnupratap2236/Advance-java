package com.shopsphere.web.customer;

import com.shopsphere.model.Address;
import com.shopsphere.model.CartView;
import com.shopsphere.service.AddressService;
import com.shopsphere.service.CartService;
import com.shopsphere.service.OrderService;
import com.shopsphere.util.Validation;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Checkout: shows the address form with the order summary and places the order.
 *
 * <p>The order is always created on the server from the current database prices and
 * the stock is re-validated inside the transaction.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends BaseServlet {

    private final CartService cartService = new CartService();
    private final AddressService addressService = new AddressService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long userId = WebContext.currentUserId(request);
            CartView cart = cartService.view(userId, paramOrNull(request, "coupon"));
            if (cart.isEmpty()) {
                WebContext.flashInfo(request, "Your cart is empty. Add a few products first.");
                redirect(request, response, "/products");
                return;
            }
            List<Address> addresses = addressService.list(userId);
            request.setAttribute("cart", cart);
            request.setAttribute("addresses", addresses);
            request.setAttribute("address", addressService.findDefault(userId).orElse(null));
            render(request, response, "checkout", "Checkout");
        } catch (Exception e) {
            handleUnexpected(request, response, e,
                    "We could not open the checkout right now. Please try again in a moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        String action = param(request, "action");
        try {
            if ("applyCoupon".equals(action)) {
                String code = param(request, "couponCode");
                CartView cart = cartService.view(userId, code);
                request.setAttribute("cart", cart);
                request.setAttribute("addresses", addressService.list(userId));
                request.setAttribute("address", addressService.findDefault(userId).orElse(null));
                request.setAttribute("couponCode", code);
                render(request, response, "checkout", "Checkout");
                return;
            }

            if (!"placeOrder".equals(action)) {
                redirect(request, response, "/checkout");
                return;
            }

            String recipientName = param(request, "recipientName");
            String phone = param(request, "phone");
            String line1 = param(request, "line1");
            String line2 = paramOrNull(request, "line2");
            String city = param(request, "city");
            String state = param(request, "state");
            String postalCode = param(request, "postalCode");
            String country = paramOrNull(request, "country");
            String notes = paramOrNull(request, "notes");
            String paymentMethod = param(request, "paymentMethod");
            String couponCode = paramOrNull(request, "couponCode");

            String error = validateCheckout(recipientName, phone, line1, city, state, postalCode,
                    paymentMethod);
            if (error != null) {
                CartView cart = cartService.view(userId, couponCode);
                request.setAttribute("cart", cart);
                request.setAttribute("addresses", addressService.list(userId));
                request.setAttribute("formError", error);
                render(request, response, "checkout", "Checkout");
                return;
            }

            OrderService.CheckoutRequest checkout = new OrderService.CheckoutRequest(
                    userId, recipientName, phone, line1, line2, city, state, postalCode,
                    country == null ? "India" : country, notes, paymentMethod, couponCode);

            try {
                var order = orderService.placeOrder(checkout);
                WebContext.setCartCount(request, 0);
                redirect(request, response, "/order/success?id=" + order.getId());
            } catch (OrderService.CheckoutException e) {
                CartView cart = cartService.view(userId, couponCode);
                request.setAttribute("cart", cart);
                request.setAttribute("addresses", addressService.list(userId));
                request.setAttribute("formError", e.getMessage());
                render(request, response, "checkout", "Checkout");
            }
        } catch (Exception e) {
            LOG.severe("Checkout failed: " + e.getMessage());
            handleUnexpected(request, response, e,
                    "We could not place the order. Nothing was charged and your cart is unchanged.");
        }
    }

    private String validateCheckout(String recipientName, String phone, String line1, String city,
                                    String state, String postalCode, String paymentMethod) {
        if (!Validation.isNotBlank(recipientName)) {
            return "Enter the name of the recipient.";
        }
        if (!Validation.isPhone(phone)) {
            return "Enter a valid contact phone number.";
        }
        if (!Validation.isNotBlank(line1)) {
            return "Enter the street address.";
        }
        if (!Validation.isNotBlank(city) || !Validation.isNotBlank(state)) {
            return "Enter the city and the state.";
        }
        if (!Validation.isNotBlank(postalCode)) {
            return "Enter the postal code.";
        }
        if (!"COD".equals(paymentMethod) && !"DEMO_CARD".equals(paymentMethod)
                && !"BANK_TRANSFER".equals(paymentMethod)) {
            return "Choose a valid payment method.";
        }
        return null;
    }
}
