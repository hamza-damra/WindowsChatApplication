import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class messageForm {
    private JTextArea messageTextArea;  // Scrollable message text area
    private JPanel panel1;
    private JButton sendMessageButton;
    private JTextField inputTextField;
    private JScrollPane messageScrollPane;

    private final PrintWriter writer;
    private final BufferedReader reader;
    private final Socket socket;

    public messageForm(String username, Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send the username immediately to the server
        writer.println(username);

        // Button click handler to send the message
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputMessage = inputTextField.getText().trim();
                if (inputMessage.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a message.");
                    return;
                }

                String message = username + ": " + inputMessage;
                writer.println(message);  // Send the message to the server
                writer.flush();
                inputTextField.setText("");  // Clear the input field
            }
        });

        // Set up the scrollable text area for message display
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setEditable(false);  // Make the text area read-only
        messageScrollPane.setViewportView(messageTextArea);

        // Start a background thread to listen for incoming messages
        listenForMessages();
    }

    // Listen for incoming messages from the server and display them in the text area
    private void listenForMessages() {
        new Thread(() -> {
            String incomingMessage;
            while (socket.isConnected()) {
                try {
                    incomingMessage = reader.readLine();
                    if (incomingMessage != null) {
                        messageTextArea.append(incomingMessage + "\n");
                        // Automatically scroll to the bottom to see the latest message
                        messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    Logger.getLogger(messageForm.class.getName()).log(Level.SEVERE, null, e);
                    break;
                }
            }
        }).start();
    }

    // Retrieve the main JPanel (used to display the GUI)
    public JPanel getPanel() {
        return panel1;
    }

    // Main entry point to launch the GUI-based client
    public static void main(String[] args) {
        // Prompt for a username, and ensure it's valid
        String username;
        while (true) {
            username = JOptionPane.showInputDialog("Enter your username:");
            if (username == null) {
                // If the user clicks "Cancel," exit the program
                System.exit(0);
            }
            if (!username.trim().isEmpty()) {
                break;
            }
            JOptionPane.showMessageDialog(null, "Please enter a valid username.");
        }

        try {
            // Establish the socket connection to the server
            Socket socket = new Socket("localhost", 8080);

            // Initialize and display the GUI form with a larger frame
            JFrame frame = new JFrame("Chat Application");
            messageForm messageForm = new messageForm(username, socket);
            frame.setContentPane(messageForm.getPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(new Dimension(1200, 800));  // Adjusted frame size to 1200x800
            frame.setLocationRelativeTo(null);  // Center the window on the screen
            frame.setVisible(true);
        } catch (IOException e) {
            Logger.getLogger(messageForm.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
