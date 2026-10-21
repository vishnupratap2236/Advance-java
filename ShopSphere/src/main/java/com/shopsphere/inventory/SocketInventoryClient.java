package com.shopsphere.inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Talks to a {@link SocketInventoryServer}. Every call has a short timeout; when the
 * remote service cannot be reached the caller is told so instead of hanging.
 */
public class SocketInventoryClient implements InventoryService {

    private static final Logger LOG = Logger.getLogger(SocketInventoryClient.class.getName());

    private final String host;
    private final int port;
    private final int timeoutMs;

    public SocketInventoryClient(String host, int port, int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public int getStock(long productId) {
        String response = send("GET " + productId);
        try {
            return response == null ? 0 : Integer.parseInt(response.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean reserve(long productId, int quantity) {
        return "OK".equals(send("RESERVE " + productId + " " + quantity));
    }

    @Override
    public void release(long productId, int quantity) {
        send("RELEASE " + productId + " " + quantity);
    }

    @Override
    public void setStock(long productId, int stock) {
        send("SET " + productId + " " + stock);
    }

    @Override
    public String describe() {
        return "Socket inventory service at " + host + ":" + port;
    }

    /** Sends one command and returns the answer, or {@code null} when unreachable. */
    public String send(String command) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            try (Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                writer.write(command);
                writer.write("\n");
                writer.flush();
                return reader.readLine();
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "The inventory service is not reachable: " + e.getMessage());
            return null;
        }
    }
}
