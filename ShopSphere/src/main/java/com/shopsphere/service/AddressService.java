package com.shopsphere.service;

import com.shopsphere.dao.AddressDao;
import com.shopsphere.model.Address;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Address book management. All operations are scoped to the owning customer.
 */
public class AddressService {

    private static final Logger LOG = Logger.getLogger(AddressService.class.getName());

    private final AddressDao addressDao = new AddressDao();

    public List<Address> list(long userId) throws SQLException {
        return addressDao.findByUser(userId);
    }

    public Optional<Address> find(long id, long userId) throws SQLException {
        return addressDao.findById(id, userId);
    }

    public Optional<Address> findDefault(long userId) throws SQLException {
        return addressDao.findDefault(userId);
    }

    /** Validates and stores a new address. */
    public Optional<Address> create(long userId, String recipientName, String phone, String line1,
                                    String line2, String city, String state, String postalCode,
                                    String country, boolean makeDefault) throws SQLException {
        if (!isValid(recipientName, phone, line1, city, state, postalCode)) {
            return Optional.empty();
        }
        Address address = new Address();
        address.setUserId(userId);
        address.setRecipientName(recipientName.trim());
        address.setPhone(phone.trim());
        address.setLine1(line1.trim());
        address.setLine2(line2 == null || line2.isBlank() ? null : line2.trim());
        address.setCity(city.trim());
        address.setState(state.trim());
        address.setPostalCode(postalCode.trim());
        address.setCountry(country == null || country.isBlank() ? "India" : country.trim());
        address.setDefaultAddress(makeDefault);

        if (makeDefault || addressDao.countForUser(userId) == 0) {
            addressDao.clearDefault(userId);
            address.setDefaultAddress(true);
        }
        long id = addressDao.insert(address);
        address.setId(id);
        return Optional.of(address);
    }

    /** Updates an address that belongs to the given customer. */
    public boolean update(long id, long userId, String recipientName, String phone, String line1,
                          String line2, String city, String state, String postalCode, String country,
                          boolean makeDefault) throws SQLException {
        if (!isValid(recipientName, phone, line1, city, state, postalCode)) {
            return false;
        }
        Optional<Address> existing = addressDao.findById(id, userId);
        if (existing.isEmpty()) {
            return false;
        }
        Address address = existing.get();
        address.setRecipientName(recipientName.trim());
        address.setPhone(phone.trim());
        address.setLine1(line1.trim());
        address.setLine2(line2 == null || line2.isBlank() ? null : line2.trim());
        address.setCity(city.trim());
        address.setState(state.trim());
        address.setPostalCode(postalCode.trim());
        address.setCountry(country == null || country.isBlank() ? "India" : country.trim());
        address.setDefaultAddress(makeDefault);
        if (makeDefault) {
            addressDao.clearDefault(userId);
        }
        return addressDao.update(address);
    }

    public boolean delete(long id, long userId) throws SQLException {
        return addressDao.delete(id, userId);
    }

    public boolean setDefault(long id, long userId) throws SQLException {
        Optional<Address> address = addressDao.findById(id, userId);
        if (address.isEmpty()) {
            return false;
        }
        addressDao.clearDefault(userId);
        address.get().setDefaultAddress(true);
        return addressDao.update(address.get());
    }

    private boolean isValid(String recipientName, String phone, String line1, String city,
                            String state, String postalCode) {
        return isFilled(recipientName, 120) && isFilled(phone, 40) && isFilled(line1, 200)
                && isFilled(city, 100) && isFilled(state, 100) && isFilled(postalCode, 20)
                && com.shopsphere.util.Validation.isPhone(phone);
    }

    private boolean isFilled(String value, int maxLength) {
        return value != null && !value.isBlank() && value.trim().length() <= maxLength;
    }
}
