import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.username = reader.readLine();
            if (username == null || username.isEmpty()) {
                username = "Unknown";
            }

            clients.add(this);
            broadcastMessage("Server: " + username + " has joined the chat");
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        try {
            while (socket.isConnected()) {
                messageFromClient = reader.readLine();
                if (messageFromClient != null) {
                    broadcastMessage(messageFromClient);
                }
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    private void broadcastMessage(String messageToSend) {
        for (ClientHandler client : clients) {
            try {
                client.writer.write(messageToSend + "\n");
                client.writer.flush();
            } catch (IOException e) {
                closeEverything(client.socket, client.reader, client.writer);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
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
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        removeClient(this);
    }

    private void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcastMessage("Server: " + client.username + " has left the chat");
    }
}
