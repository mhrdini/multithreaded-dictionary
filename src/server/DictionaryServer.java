package server;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer extends JFrame {

    private JPanel mainPanel;
    private JLabel dictionaryServerLabel;
    private JTextArea recordLogTextArea;

    public DictionaryServer(String title) {
        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
    }

    public JTextArea getRecordLog() {
        return recordLogTextArea;
    }

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        JFrame frame = new DictionaryServer("Multi-threaded Dictionary Server");
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(600, 420);

        try {

            serverSocket = new ServerSocket(3000);

            while (true) {
                // TODO: Create thread to do multithreaded functions
                //clientSocket = serverSocket.accept();
                //Thread worker = new Thread();
                //worker.start();
            }

        } catch (IOException e) {
            System.out.println("Error setting up server socket.");
        }
    }


}
