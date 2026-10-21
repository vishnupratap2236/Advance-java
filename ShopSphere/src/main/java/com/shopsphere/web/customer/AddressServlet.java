package com.shopsphere.web.customer;

import com.shopsphere.service.AddressService;
import com.shopsphere.util.WebContext;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The address book of the signed in customer.
 */
@WebServlet(name = "AddressServlet", urlPatterns = {"/account/addresses"})
public class AddressServlet extends BaseServlet {

    private final AddressService addressService = new AddressService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = WebContext.currentUserId(request);
        String action = param(request, "action");
        try {
            switch (action) {
                case "add" -> {
                    boolean created = addressService.create(userId,
                            param(request, "recipientName"), param(request, "phone"),
                            param(request, "line1"), paramOrNull(request, "line2"),
                            param(request, "city"), param(request, "state"),
                            param(request, "postalCode"), paramOrNull(request, "country"),
                            paramBoolean(request, "isDefault")).isPresent();
                    if (created) {
                        redirectWithSuccess(request, response, "/account/addresses",
                                "The address was saved.");
                    } else {
                        redirectWithError(request, response, "/account/addresses",
                                "Please check the address details and try again.");
                    }
                }
                case "update" -> {
                    long id = paramLong(request, "id", -1);
                    boolean updated = addressService.update(id, userId,
                            param(request, "recipientName"), param(request, "phone"),
                            param(request, "line1"), paramOrNull(request, "line2"),
                            param(request, "city"), param(request, "state"),
                            param(request, "postalCode"), paramOrNull(request, "country"),
                            paramBoolean(request, "isDefault"));
                    if (updated) {
                        redirectWithSuccess(request, response, "/account/addresses",
                                "The address was updated.");
                    } else {
                        redirectWithError(request, response, "/account/addresses",
                                "The address could not be updated. Please check the details.");
                    }
                }
                case "delete" -> {
                    long id = paramLong(request, "id", -1);
                    boolean deleted = addressService.delete(id, userId);
                    if (deleted) {
                        redirectWithSuccess(request, response, "/account/addresses",
                                "The address was deleted.");
                    } else {
                        redirectWithError(request, response, "/account/addresses",
                                "The address could not be deleted.");
                    }
                }
                case "setDefault" -> {
                    long id = paramLong(request, "id", -1);
                    addressService.setDefault(id, userId);
                    redirectWithSuccess(request, response, "/account/addresses",
                            "The default address was updated.");
                }
                default -> redirect(request, response, "/account/addresses");
            }
        } catch (Exception e) {
            handleUnexpected(request, response, e, "We could not update your address book.");
        }
    }
}
