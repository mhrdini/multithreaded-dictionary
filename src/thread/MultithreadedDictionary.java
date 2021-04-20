/***
 * Student Name: Mahardini Rizky Putri
 * Student ID: 921790
 * Date Created: 15 April 2020
 */

package thread;

import server.DictionaryServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultithreadedDictionary extends Thread {

    private final DictionaryServer server;
    private final String dictionaryFile;

    private final Socket clientSocket;
    private final int clientNumber;

    private static final String ADD_COMMAND = "add";
    private static final String SEARCH_COMMAND = "search";
    private static final String UPDATE_COMMAND = "update";
    private static final String DELETE_COMMAND = "delete";

    private static final String COMMA_DELIMITER = ",";
    private static final String SEMICOLON_DELIMITER = ";";
    private static final String MESSAGE_DELIMITER = ">";

    private static final String DEFAULT_DICTIONARY_FILENAME = "dictionary.csv";
    private static final int NUMBER_OF_DICTIONARY_TOKENS_PER_LINE = 2;

    private static final String SUCCESS_SUFFIX = " -> SUCCESS";
    private static final String ERROR_SUFFIX = " -> ERROR";

    public MultithreadedDictionary(Socket clientSocket,
                                   int clientNumber,
                                   DictionaryServer server,
                                   String dictionaryFile) {

        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.server = server;
        this.dictionaryFile = dictionaryFile;
    }

    /***
     * The run function of this thread.
     */
    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                     clientSocket.getOutputStream(), StandardCharsets.UTF_8));) {

            // If the input dictionary
            if (dictionaryFile.isEmpty()) {
                File file = new File(DEFAULT_DICTIONARY_FILENAME);
                if (file.createNewFile()) {
                    server.addToRecordLog("Created an empty dictionary file (CSV) " +
                                            "in current directory: dictionary.csv");
                } else {
                    throw new Exception("Error creating dictionary file.");
                }
            }

            if (!dictionaryFile.endsWith(".csv")) {
                server.addToRecordLog("Incompatible dictionary file format ("+ dictionaryFile +
                                        "). Server only accepts .csv files.");
                File file = new File(DEFAULT_DICTIONARY_FILENAME);
                if (file.createNewFile()) {
                    server.addToRecordLog("Created an empty dictionary file (CSV): dictionary.txt");
                } else {
                    throw new Exception("Error creating dictionary file.");
                }
            }

            String clientMessage;

            while ((clientMessage = in.readLine()) != null) {

                // Messages from the client are received in the format:
                // (command)>(word)>(semicolon-separated definitions) without the parentheses
                List<String> tokens = Arrays.asList(clientMessage.split(MESSAGE_DELIMITER));
                String command = tokens.get(0);
                String word = tokens.get(1).toLowerCase();
                String message = tokens.get(2);

                // Adding a new word and its definition(s)
                switch (command) {
                    case ADD_COMMAND:
                        HashMap<String, List<String>> addDictionary = readCSVToHashMap(dictionaryFile);
                        add(out, word, message, addDictionary);
                        break;
                    case SEARCH_COMMAND:
                        HashMap<String, List<String>> searchDictionary = readCSVToHashMap(dictionaryFile);
                        search(out, word, searchDictionary);
                        break;
                    case UPDATE_COMMAND:
                        HashMap<String, List<String>> updateDictionary = readCSVToHashMap(dictionaryFile);
                        update(out, word, message, updateDictionary);
                        break;
                    case DELETE_COMMAND:
                        HashMap<String, List<String>> deleteDictionary = readCSVToHashMap(dictionaryFile);
                        delete(out, word, deleteDictionary);
                        break;
                }

            }

        } catch (IOException e) {
            server.addToRecordLog("Error reading/writing stream.");
        } catch (Exception e) {
            server.addToRecordLog("Error operating server.");
        } finally {
            server.addToRecordLog("Connection with client "+ clientNumber +" is now closed.");
        }

    }

    /***
     * Reads a CSV file in the root directory, whose lines contain a String for the word, and a String
     * for its definition(s) that is separated by semicolons (;) if there are multiple definitions,
     * into a HashMap to be used as the internal server memory of the dictionary.
     *
     * @param fileName the name of the dictionary file in the root directory of the project files
     * @return HashMap containing the dictionary String entries and the list of its String definitions
     */
    public HashMap<String, List<String>> readCSVToHashMap(String fileName) {

        HashMap<String, List<String>> dictionary = null;

        // Reads the CSV dictionary file where the first token is the word
        // and the second token is the word's definitions separated by semicolons
        String root = System.getProperty("user.dir");
        String filePath = root + File.separator + fileName;

        try (BufferedReader in = new BufferedReader(new FileReader(filePath))) {

            // Store word as key and the list of its meanings as its value
            dictionary = new HashMap<>();

            String line;

            // Each comma-separated line constitutes a word and its definitions separated by semicolons
            while ((line = in.readLine()) != null) {

                List<String> wordDetails = Arrays.asList(line.split(COMMA_DELIMITER));

                // Ignore incorrectly formatted lines
                if (wordDetails.size() == NUMBER_OF_DICTIONARY_TOKENS_PER_LINE) {

                    // All words are case-insensitive and become lower-cased
                    String word = wordDetails.get(0).toLowerCase();

                    // Ignore lines with empty words
                    if (!word.equals("")) {
                        List<String> definitions = Arrays.asList(wordDetails.get(1).split(SEMICOLON_DELIMITER));

                        // Ignore lines with empty definitions
                        if (!definitions.isEmpty()) {

                            // Add entry if a new word is encountered in file,
                            // Replace definitions if an existing word is encountered in file
                            if (dictionary.containsKey(word)) {
                                dictionary.replace(word, definitions);
                            } else {
                                dictionary.put(word, definitions);
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            server.addToRecordLog("Error file not found.");
        } catch (IOException e) {
            server.addToRecordLog("Error reading file.");
        }

        return dictionary;
    }

    /***
     * Writes the HashMap word-definition(s) entries corresponding to the internal server memory of the dictionary
     * to a file in the root directory with a given filename.
     *
     * Each entry is written as a comma-separated line, where the first value is the String word (key) and
     * the second is a single String containing the concatenated definitions (value)
     * of the word, separated by semicolons (;).
     *
     * @param dictionary
     * @param fileName
     */
    public void writeHashMapToCsv(HashMap<String, List<String>> dictionary, String fileName) {

        String root = System.getProperty("user.dir");
        String filePath = root + File.separator + fileName;

        try (FileWriter out = new FileWriter(filePath)) {

            for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
                List<String> definitions = entry.getValue();
                String singleDefinitionString = definitions.stream().reduce((definition1, definition2) -> definition1 +
                                                SEMICOLON_DELIMITER + definition2).get();
                out.write(entry.getKey() + COMMA_DELIMITER + singleDefinitionString + "" + "\n");
            }

            out.flush();
        } catch (Exception e) {
            server.addToRecordLog("Error writing to the dictionary.");
        }

    }

    /***
     * Synchronised method for thread to add a word the dictionary in the internal server memory.
     * If word already exists, an error message is sent. Otherwise, the dictionary will be updated.
     *
     * The resultant dictionary will be written to a local file in the root directory.
     *
     * @param out BufferedWriter object serving as channel to respond stream to client
     * @param word String containing the word to be added
     * @param message String containing the concatenated, semicolon-separated definitions of the word
     * @param dictionary HashMap to contain String words as keys and List of String definitions
     * @throws IOException
     */
    private synchronized void add(BufferedWriter out, String word, String message,
                                  HashMap<String, List<String>> dictionary) throws IOException {

        if (dictionary.containsKey(word)) {
            server.addToRecordLog("Client "+ clientNumber +" to ADD: " + word + ERROR_SUFFIX);
            out.write("Word already exists." + "\n");

        } else {
            List<String> definitions = Arrays.asList(message.split(SEMICOLON_DELIMITER));
            dictionary.put(word, definitions);

            server.addToRecordLog("Client "+ clientNumber +" to ADD: " + word + SUCCESS_SUFFIX);

            writeHashMapToCsv(dictionary, dictionaryFile);
            out.write("Dictionary successfully updated!" + "\n");
        }

        out.flush();

    }

    /***
     * Synchronised method for thread to search for a word and its definitions in the internal dictionary.
     * If word is found, the list of its definitions will be concatenated together, separated by semicolons (;)
     * and sent to the client. Otherwise, an error message is sent.
     *
     * @param out BufferedWriter object serving as channel to respond stream to client
     * @param word String containing the word to be searched for
     * @param dictionary HashMap to contain String words as keys and List of String definitions
     * @throws IOException
     */
    private void search(BufferedWriter out, String word, HashMap<String, List<String>> dictionary) throws IOException {

        if (dictionary.containsKey(word)) {
            List<String> definitions = dictionary.get(word);
            server.addToRecordLog("Client "+ clientNumber +" to SEARCH: " + word + SUCCESS_SUFFIX);

            String singleDefinitionString = definitions.stream().reduce((definition1, definition2) -> definition1 +
                                            SEMICOLON_DELIMITER + definition2).get();

            out.write( singleDefinitionString + "\n");

        } else {
            server.addToRecordLog("Client "+ clientNumber +" to SEARCH: " + word + ERROR_SUFFIX);

            out.write("Word does not exist." + "\n");
        }

        out.flush();

    }

    /***
     * Synchronised method for thread to update a word's definition in the internal dictionary.
     * If the word exists, the input message will be converted from a single String of semicolon-separated definitions
     * to a list of String definitions, which will be set as the word's value in the dictionary.
     * Otherwise, an error is sent back to the client.
     *
     * The resultant dictionary will be written to a local file in the root directory.
     *
     * @param out BufferedWriter object serving as channel to respond stream to client
     * @param word String containing the word to be updated
     * @param message String containing the concatenated, semicolon-separated definitions of the word
     * @param dictionary HashMap to contain String words as keys and List of String definitions
     * @throws IOException
     */
    private synchronized void update(BufferedWriter out, String word, String message,
                                     HashMap<String, List<String>> dictionary) throws IOException {

        if (dictionary.containsKey(word)) {
            List<String> definitions = Arrays.asList(message.split(SEMICOLON_DELIMITER));
            dictionary.replace(word, definitions);

            server.addToRecordLog("Client "+ clientNumber +" to UPDATE: " + word + SUCCESS_SUFFIX);

            writeHashMapToCsv(dictionary, dictionaryFile);
            out.write("Dictionary successfully updated!" + "\n");

        } else {
            server.addToRecordLog("Client "+ clientNumber +" to UPDATE: " + word + ERROR_SUFFIX);

            out.write("Word does not exist. A non-existent word cannot be updated." + "\n");
        }

        out.flush();

    }

    /***
     * Synchronised method for thread to delete a word from the internal dictionary.
     * If the word exists, the word is simply deleted. Otherwise, an error is sent back to the client.
     *
     * The resultant dictionary will be written to a local file in the root directory.
     *
     * @param out BufferedWriter object serving as channel to respond stream to client
     * @param word String containing the word to be added
     * @param dictionary HashMap to contain String words as keys and List of String definitions
     * @throws IOException
     */
    private synchronized void delete(BufferedWriter out, String word,
                                     HashMap<String, List<String>> dictionary) throws IOException {

        if (dictionary.containsKey(word)) {
            dictionary.remove(word);
            server.addToRecordLog("Client "+ clientNumber +" to DELETE: " + word + SUCCESS_SUFFIX);

            writeHashMapToCsv(dictionary, dictionaryFile);
            out.write("Dictionary successfully updated!" + "\n");
        } else {
            server.addToRecordLog("Client "+ clientNumber +" to DELETE: " + word + ERROR_SUFFIX);

            out.write("Word does not exist.\nA non-existent word cannot be deleted." + "\n");
        }

        out.flush();

    }
}
