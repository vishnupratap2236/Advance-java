package com.shopsphere.model;

/** Role of an authenticated user. */
public enum Role {
    CUSTOMER,
    ADMIN;

    public static Role fromString(String value) {
        if (value == null) {
            return CUSTOMER;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return CUSTOMER;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
