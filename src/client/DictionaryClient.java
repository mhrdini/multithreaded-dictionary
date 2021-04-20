/***
 * Student Name: Mahardini Rizky Putri
 * Student ID: 921790
 * Date Created: 15 April 2020
 */

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DictionaryClient extends JFrame {

    private JPanel mainPanel;
    private JPanel searchDeletePanel;
    private JTabbedPane tabs;
    private JPanel addUpdatePanel;
    private JButton addButton;
    private JTextArea addUpdateTextArea;
    private JLabel addUpdateInstructionsLabel;
    private JTextField searchDeleteTextField;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JLabel addUpdateErrorLabel;
    private JTextArea searchResultsTextArea;
    private JTextField addUpdateTextField;
    private JLabel searchDeleteErrorLabel;

    private static final String ADD_COMMAND = "add";
    private static final String SEARCH_COMMAND = "search";
    private static final String UPDATE_COMMAND = "update";
    private static final String DELETE_COMMAND = "delete";

    private static final String MESSAGE_CARET = " > ";
    private static final String SEMICOLON_DELIMITER = ";";
    private static final String MESSAGE_DELIMITER = ">";

    private static final String HOST_OPTION = "-h";
    private static final String PORT_OPTION = "-p";

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3000;

    private static final int MIN_PORT_NUM = 1024;
    private static final int MAX_PORT_NUM = 65535;

    /***
     * Constructor for DictionaryClient.
     * @param title The title of the Client GUI
     * @param host The remote host for the client socket
     * @param port The local port for the client socket
     */
    public DictionaryClient (String title, String host, int port) {
        super(title);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setContentPane(mainPanel);
        pack();

        addUpdateInstructionsLabel.setText("Write word definition(s) below. " +
                                           "Separate multiple definitions with semicolons (;).");

        // Each command button invokes the corresponding command method when clicked
        addButton.addActionListener(e -> addWord(host, port));
        searchButton.addActionListener(e -> searchWord(host, port));
        updateButton.addActionListener(e -> updateWord(host, port));
        deleteButton.addActionListener(e -> deleteWord(host, port));

        // Clears the error messages when another tab is clicked
        tabs.addChangeListener(e -> {
            searchDeleteErrorLabel.setText("");
            addUpdateErrorLabel.setText("");
        });

        // When the close button is clicked, an option pane appears to asks the user to confirm whether
        // or not they want to exit the application. Choosing Yes will close the application.
        // Since default close operation is set to do nothing on close, choosing No will do nothing.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int n = JOptionPane.showConfirmDialog(null,
                        "Would you like to exit the application?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    System.exit(0);
                }
            }
        });

    }

    /***
     * Main function of the dictionary client.
     *
     * @param args Command line arguments of the client.
     */
    public static void main(String[] args) {

        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;

        // Parsing the argument options for the client host and port for the client socket

        try {
            Iterator<String> it = Arrays.asList(args).iterator();

            while (it.hasNext()) {
                String option = it.next();

                switch (option) {
                    case HOST_OPTION:
                        if (it.hasNext()) host = it.next();
                        break;
                    case PORT_OPTION:
                        if (it.hasNext()) port = Integer.parseInt(it.next());
                        if (port < MIN_PORT_NUM || port > MAX_PORT_NUM) port = DEFAULT_PORT;
                        break;
                    default:
                        break;
                }

            }
        } catch (NumberFormatException e) {
            System.out.println("Non-integer port value for Server. Fallback to port "+DEFAULT_PORT+".");
        }

        // Initialise and launch the Client GUI
        // The client host and port are part of the constructor since the values will be used in the
        // add, search, update, and delete methods to create a socket for the request.
        DictionaryClient client = new DictionaryClient("Multi-threaded Dictionary Client", host, port);
        client.setVisible(true);
        client.setResizable(false);
        client.setSize(600, 420);
        Toolkit toolKit = client.getToolkit();
        Dimension size = toolKit.getScreenSize();
        client.setLocation(size.width/2 - client.getWidth()/2, size.height/2 - client.getHeight()/2);

    }

    // **** Methods for sending and receiving information through Socket I/O Stream **** //

    /**
     * Uses the given socket to send out a message to the connected server.
     * @param socket Socket to serve as gateway of communication channel
     * @param message String message to send
     * @return true if message was sent out, false otherwise.
     */
    public boolean outputSocketStream(Socket socket, String message) {
        BufferedWriter out;
        try  {
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(message);
            out.flush();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Uses the given socket to receive a message to the connected server.
     * @param socket Socket to serve as gateway of communication channel
     * @return true if message received, false otherwise.
     */
    public String inputSocketStream(Socket socket) {
        BufferedReader in;
        try  {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String result = in.readLine();
            return result;
        }
        catch (IOException e) {
            return null;
        }
    }

    // **** Methods for dictionary commands **** //

    /***
     * Client method that attempts to add a word and its definitions to the dictionary in the server.
     * Ensures that the word and its definitions are both non-empty.
     * Creates a temporary socket for sending the command, word, and its definitions to the server.
     * If the sending operation is successful, then it attempts to receive the response from the server.
     *
     * @param host The host of the client socket
     * @param port The port of the client socket
     */
    public void addWord(String host, int port) {

        if (!addUpdateTextField.getText().equals("")) {

            if (!addUpdateTextArea.getText().equals("")) {

                try (Socket socket = new Socket(host, port)) {

                    List<String> messageList = Arrays.asList(new String[]{ADD_COMMAND,
                                                                            addUpdateTextField.getText(),
                                                                            addUpdateTextArea.getText()});
                    String message = messageList.stream()
                                                .reduce((message1, message2) -> message1 +
                                                        MESSAGE_DELIMITER + message2).get() + "\n";

                    if (outputSocketStream(socket, message)) {
                        String result = inputSocketStream(socket);

                        if (result != null) {
                            addUpdateErrorLabel.setText(result);
                        } else {
                            System.out.println("Error adding new word.");
                        }

                    }
                } catch(ConnectException e){
                    handleConnectException();
                } catch(UnknownHostException e){
                    handleUnknownHostException();
                } catch(IOException e){
                    handleIOException();
                }

            } else {
                addUpdateErrorLabel.setText("ERROR: Please enter the word's definition(s).");
            }

        } else {
            addUpdateErrorLabel.setText("ERROR: Please enter the word you want to add.");
        }

    }

    /***
     * Client method that attempts to search for a word's definitions from the dictionary in the server.
     * Ensures that the to-be-searched word is non-empty.
     * Creates a temporary socket for sending the command and the word to the server.
     * If the sending operation is successful, then it attempts to receive the response from the server.
     *
     * @param host The host of the client socket
     * @param port The port of the client socket
     */
    public void searchWord(String host, int port) {

        if (!searchDeleteTextField.getText().equals("")) {

            try (Socket socket = new Socket(host, port)){

                List<String> messageList = Arrays.asList(new String[]{SEARCH_COMMAND,
                                                                        searchDeleteTextField.getText(), " "});
                String message = messageList.stream()
                                            .reduce((message1, message2) -> message1 +
                                                    MESSAGE_DELIMITER + message2).get() + "\n";

                if (outputSocketStream(socket, message)) {

                    String result = inputSocketStream(socket);

                    if (result != null) {

                        if (result.equals("Word does not exist.")) {
                            searchDeleteErrorLabel.setText(result);
                        } else {
                            List<String> definitions = Arrays.asList(result.split(SEMICOLON_DELIMITER));
                            String text = definitions.stream()
                                                    .map(definition -> MESSAGE_CARET + definition)
                                                    .reduce((definition1, definition2) -> definition1 + "\n" +
                                                                                        definition2).get();
                            searchResultsTextArea.setText(text);
                        }

                    } else {
                        System.out.println("Error searching for existing word.");
                    }

                }

            } catch (ConnectException e) {
                handleConnectException();
            } catch (UnknownHostException e) {
                handleUnknownHostException();
            } catch (IOException e) {
                handleIOException();
            }

        } else {
            searchDeleteErrorLabel.setText("ERROR: Please enter the word to be searched.");
        }

    }

    /***
     * Client method that attempts to update a word's definitions in the dictionary in the server.
     * Ensures that the to-be-updated word and its new definitions are non-empty.
     * Creates a temporary socket for sending the command, word, and definitions to the server.
     * If the sending operation is successful, then it attempts to receive the response from the server.
     *
     * @param host The host of the client socket
     * @param port The port of the client socket
     */
    public void updateWord(String host, int port) {

        if (!addUpdateTextField.getText().equals("")) {

            if (!addUpdateTextArea.getText().equals("")) {

                try (Socket socket = new Socket(host, port)) {

                    List<String> messageList = Arrays.asList(new String[]{UPDATE_COMMAND,
                                                                            addUpdateTextField.getText(),
                                                                            addUpdateTextArea.getText()});
                    String message = messageList.stream()
                                                .reduce((message1, message2) -> message1 +
                                                        MESSAGE_DELIMITER + message2).get() + "\n";

                    if (outputSocketStream(socket, message)) {
                        String result = inputSocketStream(socket);

                        if (result != null) {
                            addUpdateErrorLabel.setText(result);
                        } else {
                            System.out.println("Error updating existing word.");
                        }

                    }

                } catch (ConnectException e) {
                    handleConnectException();
                } catch (UnknownHostException e) {
                    handleUnknownHostException();
                } catch (IOException e) {
                    handleIOException();
                }

            } else {
                addUpdateErrorLabel.setText("ERROR: Please enter the word's definition(s).");
            }
        } else {
            addUpdateErrorLabel.setText("ERROR: Please enter the word you want to update.");
        }

    }

    /***
     * Client method that attempts to delete a word and its definitions from the dictionary in the server.
     * Ensures that the to-be-deleted word is non-empty.
     * Creates a temporary socket for sending the command and the word to the server.
     * If the sending operation is successful, then it attempts to receive the response from the server.
     *
     * @param host The host of the client socket
     * @param port The port of the client socket
     */
    public void deleteWord(String host, int port) {

        if (!searchDeleteTextField.getText().equals("")) {

            try (Socket socket = new Socket(host, port)) {


                List<String> messageList = Arrays.asList(new String[]{DELETE_COMMAND,
                                                                        searchDeleteTextField.getText(),
                                                                        " "});
                String message = messageList.stream()
                                            .reduce((message1, message2) -> message1 +
                                                    MESSAGE_DELIMITER + message2).get() + "\n";

                if (outputSocketStream(socket, message)) {

                    String result = inputSocketStream(socket);

                    if (result != null) {
                        searchDeleteErrorLabel.setText(result);

                    } else {
                        System.out.println("Error deleting existing word.");
                    }

                }


            } catch (ConnectException e) {
                handleConnectException();
            } catch (UnknownHostException e) {
                handleUnknownHostException();
            } catch (IOException e) {
                handleIOException();
            }

        } else {
            searchDeleteErrorLabel.setText("ERROR: Please enter the word to be deleted.");
        }

    }

    // **** Methods for handling exceptions **** //

    /***
     * When the client cannot connect to the server, a dialog will pop up saying the connection has failed.
     */
    private void handleConnectException() {
        JOptionPane.showConfirmDialog(null,
                "The connection to the server has been refused.", "Error",
                JOptionPane.CLOSED_OPTION);
    }

    /***
     * When the IP address of the remote host cannot be determined by the Socket class, a dialog will pop up saying so
     */
    private void handleUnknownHostException() {
        JOptionPane.showConfirmDialog(null,
                "The IP address of the remote host could not be determined.", "Error",
                JOptionPane.CLOSED_OPTION);

    }

    /***
     * General exception for problems in the command method.
     */
    private void handleIOException() {
        JOptionPane.showConfirmDialog(null,
                "Failure in operating the client socket/command.", "Error",
                JOptionPane.CLOSED_OPTION);

    }

}
