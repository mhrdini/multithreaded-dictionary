/***
 * Student Name: Mahardini Rizky Putri
 * Student ID: 921790
 * Date Created: 15 April 2020
 */

package server;

import thread.MultithreadedDictionary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;

public class DictionaryServer extends JFrame {

    private JPanel mainPanel;
    private JTextArea recordLogTextArea;

    private static final String MESSAGE_CARET = " > ";

    private static final String PORT_OPTION = "-p";
    private static final String FILE_OPTION = "-f";

    private static final int DEFAULT_PORT = 3000;
    private static final String DEFAULT_DICTIONARY = "";

    private static final int MIN_PORT_NUM = 1024;
    private static final int MAX_PORT_NUM = 65535;

    public DictionaryServer(String title) {
        super(title);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setContentPane(mainPanel);
        pack();

        // When the close button is clicked, an option pane appears to asks the user to confirm whether
        // or not they want to exit the application. Choosing Yes will close the application.
        // Since default close operation is set to do nothing on close, choosing No will do nothing.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int n = JOptionPane.showConfirmDialog(null,
                        "Would you like to shut down the server?", "Confirm Shutdown",
                        JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    System.exit(0);
                }
            }
        });
    }

    /***
     * Main function of the dictionary server.
     *
     * @param args Command line arguments for the server
     */
    public static void main(String[] args) {

        // Parsing the argument options for the server port for the server socket and
        // the initial dictionary file to be loaded into the server

        int port = DEFAULT_PORT;
        String dictionaryFile = DEFAULT_DICTIONARY;

        try {

            Iterator<String> it = Arrays.asList(args).iterator();

            while (it.hasNext()) {
                String option = it.next();
                String tempNext;

                switch (option) {
                    case PORT_OPTION:
                        if (it.hasNext()) port = Integer.parseInt(it.next());
                        if (port < MIN_PORT_NUM || port > MAX_PORT_NUM) port = DEFAULT_PORT;
                        break;
                    case FILE_OPTION:
                        if (it.hasNext() && (tempNext = it.next()).endsWith(".csv")) dictionaryFile = tempNext;
                        break;
                    default:
                        break;
                }

            }
        } catch (NumberFormatException e) {
            System.out.println("Non-integer port value for Server. Fallback to port "+DEFAULT_PORT+".");
        }

        // Variables to store server and incoming clients
        Socket clientSocket;

        // Initialise and launch the Server GUI
        DictionaryServer server = new DictionaryServer("Multi-threaded Dictionary Server");
        server.setVisible(true);
        server.setResizable(false);
        server.setSize(600, 420);
        Toolkit toolKit = server.getToolkit();
        Dimension size = toolKit.getScreenSize();
        server.setLocation(size.width/2 - server.getWidth()/2, size.height/2 - server.getHeight()/2);


        try (ServerSocket serverSocket = new ServerSocket(port)) {

            // Open the server socket to listen to incoming connections
            server.addToRecordLog("Each incoming connection will have: (datetime, remote host, " +
                                               "remote port, local port)");
            server.addToRecordLog("Server listening for connections on port " +  port + "...");

            int clientNumber = 0;

            // Indefinitely listen for and accept incoming client connections, creating and starting a thread for each.
            // Log the incoming connection in the server GUI.
            while (true) {

                clientSocket = serverSocket.accept();
                Thread t = new Thread(new MultithreadedDictionary(clientSocket, clientNumber,  server, dictionaryFile));
                server.addToRecordLog("Connected with client " + clientNumber + " (" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) +
                                ", " + clientSocket.getInetAddress().getHostName() +
                                ", " + clientSocket.getPort() +
                                ", " + clientSocket.getLocalPort());
                clientNumber++;
                t.start();

            }

        } catch (IOException e) {
            System.out.println("Error setting up server socket.");
        }
    }

    /***
     * Formats and logs the message to the server GUI.
     *
     * @param logMessage the message to be logged
     */
    public void addToRecordLog(String logMessage) {
        recordLogTextArea.append(MESSAGE_CARET + logMessage + "\n");
    }

}
