import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.*;

public class Client {
    // Network socket to communicate with the server
    private Socket socket;
    // Input stream to receive messages from the server
    private BufferedReader reader;
    // Output stream to send messages to the server
    private PrintWriter writer;
    // Username of the client
    private String username;

    // Constructor to initialize the client object with a given socket and username
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            // Initialize the reader and writer to handle input/output via the socket
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            // Send the username immediately to the server after connection
            writer.println(username);
            writer.flush();
        } catch (IOException e) {
            // Close everything in case of an error
            closeEverything(socket, reader, writer);
        }
    }

    // Method to safely close the socket, reader, and writer
    private void closeEverything(Socket socket, BufferedReader reader, PrintWriter writer) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Continuously send messages from the client to the server
    public void sendMessage() {
        Scanner scanner = new Scanner(System.in);
        // Continuously read messages from the console and send them to the server
        while (scanner.hasNextLine()) {
            String message = username + ": " + scanner.nextLine();  // Prepend username to message
            writer.println(message);  // Send the formatted message to the server
            writer.flush();
        }
    }

    // Continuously listen for incoming messages from the server and print them
    public void listenForMessage() {
        // Start a new thread to receive and display messages
        new Thread(() -> {
            String messageFromGroupChat;
            while (socket.isConnected()) {
                try {
                    // Read the next message from the server
                    messageFromGroupChat = reader.readLine();
                    // Print the message to the console
                    System.out.println(messageFromGroupChat);
                } catch (IOException e) {
                    // Close resources on error
                    closeEverything(socket, reader, writer);
                }
            }
        }).start();
    }

    // Main entry point of the client application
    public static void main(String[] args) {
        // Prompt the user for a username
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter your username: ");
        String username = scanner.nextLine();

        try {
            // Connect to the server on localhost at port 8080
            Socket socket = new Socket("localhost", 8080);
            // Initialize a new client with the provided username
            Client client = new Client(socket, username);
            // Start listening for incoming messages
            client.listenForMessage();
            // Start sending messages
            client.sendMessage();
        } catch (IOException e) {
            // Log any errors that occur during connection or communication
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
