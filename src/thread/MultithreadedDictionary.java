package thread;

import server.DictionaryServer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.Stream;

public class MultithreadedDictionary extends Thread {

    private DictionaryServer server = null;
    private File dictionaryFile = null;

    private Socket clientSocket = null;
    private int clientNumber = 0;

    private final String ADD_COMMAND = "add";
    private final String SEARCH_COMMAND = "search";
    private final String UPDATE_COMMAND = "update";
    private final String DELETE_COMMAND = "delete";

    private final String COMMA_DELIMITER = ",";
    private final String SEMICOLON_DELIMITER = ";";


    public MultithreadedDictionary(Socket clientSocket,
                                   int clientNumber,
                                   DictionaryServer server,
                                   File dictionaryFile) {

        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.server = server;
        this.dictionaryFile = dictionaryFile;
    }

    @Override
    public void run() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));

            String clientMessage;

            while ((clientMessage = in.readLine()) != null) {
                String[] tokens = clientMessage.split(">");
                String command = tokens[0];
                String word = tokens[1];
                String message = tokens[2];
                HashMap<String, String[]> dictionary = null;

                if (command == ADD_COMMAND) {

                    dictionary = readDictionaryCSVToHashMap();
                    add(out, word, message, dictionary);

                } else if (command == SEARCH_COMMAND) {

                    dictionary = readDictionaryCSVToHashMap();
                    search(out, word, message, dictionary);

                } else if (command == UPDATE_COMMAND) {

                    dictionary = readDictionaryCSVToHashMap();
                    update(out, word, message, dictionary);

                } else if (command == DELETE_COMMAND) {

                    dictionary = readDictionaryCSVToHashMap();
                    update(out, word, message, dictionary);

                }
            }

        } catch (IOException e) {
            server.getRecordLog().append("Error reading/writing stream.\n");
        }
    }

    public HashMap<String, String[]> readDictionaryCSVToHashMap() {

        BufferedReader br = null;
        HashMap<String, String[]> dictionary = null;

        try {

            // Reads the CSV dictionary file where the first token is the word
            // and the second token is the word's definitions separated by semicolons
            br = new BufferedReader(new FileReader(dictionaryFile));

            // Store word as key and the list of its meanings as its value
            dictionary = new HashMap<String, String[]>();

            String line = "";

            while ((line = br.readLine()) != null) {

                String[] wordDetails = line.split(COMMA_DELIMITER);

                // Ensuring that a word has a definition
                if (wordDetails.length > 0) {
                    String word = wordDetails[0];
                    String[] definitions = wordDetails[1].split(SEMICOLON_DELIMITER);

                    if (dictionary.containsKey(word)) {
                        dictionary.replace(word, definitions);
                    } else {
                        dictionary.put(word, definitions);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error file not found.");
        } catch (IOException e) {
            System.out.println("Error while reading file.");
        } finally {
            try {
                br.close();
            } catch(IOException e) {
                System.out.println("Error while closing the BufferedReader.");
            }
        }

        return dictionary;
    }

    public void writeDictionaryHashMapToCsv(HashMap<String, String[]> dictionary) {

        FileWriter fw = null;

        try {
            fw = new FileWriter(dictionaryFile, false);

            for (String word : dictionary.keySet()) {
                List<String> definitions = Arrays.asList(dictionary.get(word));
                Optional<String> singleDefinitionString = definitions.stream().reduce((definition1, definition2) -> definition1 + SEMICOLON_DELIMITER + definition2);
                fw.write(word + COMMA_DELIMITER + singleDefinitionString + "\n");
            }

            fw.flush();
            fw.close();
        } catch (NoSuchElementException e) {
            System.out.println("Error writing to the dictionary.");
        } catch (IOException e) {
            System.out.println("Error writing to the dictionary.");
        }
    }

    public synchronized void add(BufferedWriter out, String word, String message, HashMap<String, String[]> dictionary) {

    }

    public synchronized void search(BufferedWriter out, String word, String message, HashMap<String, String[]> dictionary) {

    }

    public synchronized void update(BufferedWriter out, String word, String message, HashMap<String, String[]> dictionary) {

    }

    public synchronized void delete(BufferedWriter out, String word, String message, HashMap<String, String[]> dictionary) {

    }
}
