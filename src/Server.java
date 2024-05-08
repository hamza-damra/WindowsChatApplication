import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A new client is connected: " + clientSocket.getInetAddress().getHostName());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
