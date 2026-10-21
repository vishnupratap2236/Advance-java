package com.shopsphere.inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small TCP server that answers inventory questions in plain text.
 *
 * <p>Run it with:
 * <pre>
 *   java -cp target/classes com.shopsphere.inventory.SocketInventoryServer 9099
 * </pre>
 *
 * <p>The protocol is one command per line:
 * <ul>
 *   <li>{@code PING} - answers {@code PONG}</li>
 *   <li>{@code GET &lt;productId&gt;} - answers the available stock</li>
 *   <li>{@code RESERVE &lt;productId&gt; &lt;quantity&gt;} - answers {@code OK} or {@code DENIED}</li>
 *   <li>{@code RELEASE &lt;productId&gt; &lt;quantity&gt;} - answers {@code OK}</li>
 *   <li>{@code SET &lt;productId&gt; &lt;stock&gt;} - answers {@code OK}</li>
 * </ul>
 */
public class SocketInventoryServer implements Runnable {

    private static final Logger LOG = Logger.getLogger(SocketInventoryServer.class.getName());

    private final int port;
    private final InventoryService inventory;
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public SocketInventoryServer(int port, InventoryService inventory) {
        this.port = port;
        this.inventory = inventory;
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9099;
        SocketInventoryServer server = new SocketInventoryServer(port, new LocalInventoryService());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }

    public void start() {
        try {
            bind();
            LOG.info("Socket inventory service listening on 127.0.0.1:" + port);
            run();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "The socket inventory service could not be started", e);
        }
    }

    /** Binds the server socket. Safe to call more than once. */
    public synchronized void bind() throws IOException {
        if (serverSocket == null || serverSocket.isClosed()) {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        }
    }

    @Override
    public void run() {
        try {
            bind();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "The socket inventory service could not bind its port", e);
            return;
        }
        while (running) {
            try (Socket client = serverSocket.accept()) {
                handle(client);
            } catch (IOException e) {
                if (running) {
                    LOG.log(Level.WARNING, "Connection problem in the inventory service", e);
                }
            }
        }
    }

    private void handle(Socket client) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             Writer writer = new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            String[] parts = line.trim().split("\\s+");
            String response;
            switch (parts[0].toUpperCase()) {
                case "PING" -> response = "PONG";
                case "GET" -> response = String.valueOf(inventory.getStock(parseLong(parts, 1)));
                case "RESERVE" -> response = inventory.reserve(parseLong(parts, 1), parseInt(parts, 2))
                        ? "OK" : "DENIED";
                case "RELEASE" -> {
                    inventory.release(parseLong(parts, 1), parseInt(parts, 2));
                    response = "OK";
                }
                case "SET" -> {
                    inventory.setStock(parseLong(parts, 1), parseInt(parts, 2));
                    response = "OK";
                }
                default -> response = "ERROR";
            }
            writer.write(response);
            writer.write("\n");
            writer.flush();
        }
    }

    private long parseLong(String[] parts, int index) {
        return parts.length > index ? Long.parseLong(parts[index]) : 0L;
    }

    private int parseInt(String[] parts, int index) {
        return parts.length > index ? Integer.parseInt(parts[index]) : 0;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not close the inventory service", e);
        }
    }
}
