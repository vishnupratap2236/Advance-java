package com.shopsphere.inventory;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface of the RMI inventory service.
 */
public interface RemoteInventoryService extends Remote {

    int getStock(long productId) throws RemoteException;

    boolean reserve(long productId, int quantity) throws RemoteException;

    void release(long productId, int quantity) throws RemoteException;

    void setStock(long productId, int stock) throws RemoteException;

    String ping() throws RemoteException;
}
