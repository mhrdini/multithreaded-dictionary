package client;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

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

    public DictionaryClient (String title) {
        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
    }

    public static void main(String[] args) {

        JFrame frame = new DictionaryClient("Multi-threaded Dictionary Client");
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(600, 420);
    }

    /**
     *
     * @param socket
     * @param message
     */
    public boolean sendStream(Socket socket, String message) {
        return true;
    }

    /**
     *
     * @param socket
     */
    public void addWord(Socket socket) {

    }

    /**
     *
     * @param socket
     */
    public void searchWord(Socket socket) {
        if (!searchDeleteTextField.getText().isEmpty()) {

        }
    }

    /**
     *
     * @param socket
     */
    public void updateWord(Socket socket) {

    }

    /**
     *
     * @param socket
     */
    public void deleteWord(Socket socket) {

    }
}
