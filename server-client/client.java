import java.io.*;
import java.net.*;

public class client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to server " + host + ":" + port);
  
            String userInput;
            while (true) {
                System.out.print("Enter message (bye to quit): ");
                userInput = console.readLine();
                if (userInput == null) {
                    break;
                }

                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server replied: " + response);

                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

