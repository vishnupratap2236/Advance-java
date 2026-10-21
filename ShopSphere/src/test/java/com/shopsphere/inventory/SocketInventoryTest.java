package com.shopsphere.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocketInventoryTest {

    /** A tiny in memory implementation used to test the transport. */
    private static class FakeInventory implements InventoryService {
        private final AtomicInteger stock = new AtomicInteger(10);

        @Override
        public int getStock(long productId) {
            return stock.get();
        }

        @Override
        public boolean reserve(long productId, int quantity) {
            return stock.addAndGet(-quantity) >= 0;
        }

        @Override
        public void release(long productId, int quantity) {
            stock.addAndGet(quantity);
        }

        @Override
        public void setStock(long productId, int newStock) {
            stock.set(newStock);
        }

        @Override
        public String describe() {
            return "fake";
        }
    }

    @Test
    @DisplayName("the socket transport answers the inventory commands")
    void talksToTheSocketServer() throws Exception {
        int port = freePort();
        SocketInventoryServer server = new SocketInventoryServer(port, new FakeInventory());
        Thread thread = new Thread(server::run);
        thread.setDaemon(true);
        thread.start();

        // Give the server a moment to bind the port.
        Thread.sleep(300);

        try {
            SocketInventoryClient client = new SocketInventoryClient("127.0.0.1", port, 2000);

            assertEquals("PONG", client.send("PING"));
            assertEquals(10, client.getStock(1));

            assertTrue(client.reserve(1, 3));
            assertEquals(7, client.getStock(1));

            client.release(1, 3);
            assertEquals(10, client.getStock(1));

            client.setStock(1, 42);
            assertEquals(42, client.getStock(1));
        } finally {
            server.stop();
        }
    }

    @Test
    @DisplayName("an unreachable service is reported instead of hanging")
    void handlesUnreachableService() throws Exception {
        int port = freePort();
        SocketInventoryClient client = new SocketInventoryClient("127.0.0.1", port, 300);

        assertNull(client.send("PING"));
        assertEquals(0, client.getStock(1));
        assertFalse(client.reserve(1, 1));
        assertNotNull(client.describe());
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
