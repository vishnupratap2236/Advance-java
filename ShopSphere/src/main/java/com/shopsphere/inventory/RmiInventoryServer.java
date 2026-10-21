package com.shopsphere.inventory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publishes the inventory service over RMI.
 *
 * <p>Run it with:
 * <pre>
 *   java -cp target/classes com.shopsphere.inventory.RmiInventoryServer 9100 ShopSphereInventory
 * </pre>
 */
public class RmiInventoryServer extends UnicastRemoteObject implements RemoteInventoryService {

    private static final Logger LOG = Logger.getLogger(RmiInventoryServer.class.getName());

    private final InventoryService inventory;

    public RmiInventoryServer(int port, InventoryService inventory) throws RemoteException {
        super(port);
        this.inventory = inventory;
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9100;
        String name = args.length > 1 ? args[1] : "ShopSphereInventory";
        try {
            RmiInventoryServer service = new RmiInventoryServer(port, new LocalInventoryService());
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(name, service);
            LOG.info("RMI inventory service published on port " + port + " as '" + name + "'");
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "The RMI inventory service could not be started", e);
        }
    }

    @Override
    public int getStock(long productId) {
        return inventory.getStock(productId);
    }

    @Override
    public boolean reserve(long productId, int quantity) {
        return inventory.reserve(productId, quantity);
    }

    @Override
    public void release(long productId, int quantity) {
        inventory.release(productId, quantity);
    }

    @Override
    public void setStock(long productId, int stock) {
        inventory.setStock(productId, stock);
    }

    @Override
    public String ping() {
        return "PONG";
    }
}
