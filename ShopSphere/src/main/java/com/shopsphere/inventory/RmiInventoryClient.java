package com.shopsphere.inventory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Talks to an inventory service published over RMI.
 */
public class RmiInventoryClient implements InventoryService {

    private static final Logger LOG = Logger.getLogger(RmiInventoryClient.class.getName());

    private final String host;
    private final int port;
    private final String name;
    private RemoteInventoryService remote;

    public RmiInventoryClient(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    private RemoteInventoryService remote() {
        if (remote == null) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, port);
                remote = (RemoteInventoryService) registry.lookup(name);
            } catch (RemoteException | NotBoundException e) {
                LOG.log(Level.FINE, "The RMI inventory service is not reachable: " + e.getMessage());
                remote = null;
            }
        }
        return remote;
    }

    @Override
    public int getStock(long productId) {
        RemoteInventoryService service = remote();
        if (service == null) {
            return 0;
        }
        try {
            return service.getStock(productId);
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override
    public boolean reserve(long productId, int quantity) {
        RemoteInventoryService service = remote();
        if (service == null) {
            return false;
        }
        try {
            return service.reserve(productId, quantity);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public void release(long productId, int quantity) {
        RemoteInventoryService service = remote();
        if (service == null) {
            return;
        }
        try {
            service.release(productId, quantity);
        } catch (RemoteException e) {
            LOG.log(Level.FINE, "Could not release the stock remotely", e);
        }
    }

    @Override
    public void setStock(long productId, int stock) {
        RemoteInventoryService service = remote();
        if (service == null) {
            return;
        }
        try {
            service.setStock(productId, stock);
        } catch (RemoteException e) {
            LOG.log(Level.FINE, "Could not set the stock remotely", e);
        }
    }

    @Override
    public String describe() {
        return "RMI inventory service " + name + " at " + host + ":" + port;
    }
}
