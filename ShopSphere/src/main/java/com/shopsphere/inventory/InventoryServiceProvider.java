package com.shopsphere.inventory;

import com.shopsphere.config.AppConfig;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Chooses the inventory implementation the application works with.
 *
 * <p>The mode is read from {@code shopsphere.inventory.mode} (or the environment
 * variable {@code SHOPSPHERE_INVENTORY_MODE}):
 * <ul>
 *   <li>{@code local} (default) - the stock is read from the database</li>
 *   <li>{@code socket} - the stock is read from a {@link SocketInventoryServer}</li>
 *   <li>{@code rmi} - the stock is read from an {@link RmiInventoryServer}</li>
 * </ul>
 */
public final class InventoryServiceProvider {

    private static final Logger LOG = Logger.getLogger(InventoryServiceProvider.class.getName());

    private static volatile InventoryService instance;

    private InventoryServiceProvider() {
    }

    public static InventoryService get() {
        InventoryService local = instance;
        if (local == null) {
            synchronized (InventoryServiceProvider.class) {
                local = instance;
                if (local == null) {
                    local = create();
                    instance = local;
                }
            }
        }
        return local;
    }

    private static InventoryService create() {
        AppConfig config = AppConfig.get();
        String mode = config.inventoryMode().toLowerCase(Locale.ROOT);
        InventoryService service = switch (mode) {
            case "socket" -> new SocketInventoryClient(config.inventoryHost(),
                    config.inventoryPort(), config.inventoryTimeoutMs());
            case "rmi" -> new RmiInventoryClient(config.inventoryHost(), config.inventoryPort(),
                    config.inventoryServiceName());
            default -> new LocalInventoryService();
        };
        LOG.info("Inventory service: " + service.describe());
        return service;
    }

    /** Visible for tests: drops the cached service. */
    public static void reset() {
        synchronized (InventoryServiceProvider.class) {
            instance = null;
        }
    }
}
