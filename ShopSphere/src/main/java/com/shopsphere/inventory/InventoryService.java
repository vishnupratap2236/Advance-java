package com.shopsphere.inventory;

/**
 * The inventory service of ShopSphere.
 *
 * <p>The original reference implementation exposed the stock of the catalogue over
 * RMI and over a plain socket so that several application instances could share a
 * single view of the inventory. This project keeps both transports and adds a local
 * implementation that reads the stock straight from the database.
 *
 * <p>Whichever implementation is used, the database transaction in
 * {@code OrderService} stays the authority for the stock; a remote service can
 * never oversell on its own.
 */
public interface InventoryService {

    /** The number of units currently available for a product. */
    int getStock(long productId);

    /**
     * Reserves units for a short period.
     *
     * @return {@code false} when not enough units are available
     */
    boolean reserve(long productId, int quantity);

    /** Gives reserved units back. */
    void release(long productId, int quantity);

    /** Overwrites the stock of a product (administration use). */
    void setStock(long productId, int stock);

    /** A short description of the transport, shown in the administration screens. */
    String describe();
}
