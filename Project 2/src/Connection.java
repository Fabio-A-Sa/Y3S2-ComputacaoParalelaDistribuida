import java.io.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Connection {

    private final int port;                                 // The port number
    private final String host;                              // The host name or IP address
    private SocketChannel socket;                           // A SocketChannel for the connection
    private final String TOKEN_PATH = "Client/tokens/";     // A path to a directory containing tokens
    private static final String DEFAULT_HOST = "localhost"; // A default host to use if none is provided
    private final long TIMEOUT = 30000;                     // Timeout to avoid slow clients in milliseconds
    private PlayerGUI playerGui;                            // A GUI to display messages
    private int authenticationOption = 0;

    // Constructor
    public Connection(int port, String host) {
        this.port = port;
        this.host = host;
    }

    // Method to start the connection
    public void start() throws IOException {
        this.socket = SocketChannel.open();             // Open a new SocketChannel
        this.socket.connect(new InetSocketAddress(this.host, this.port)); // Connect to the specified host and port
    }

    // Method to stop the connection
    public void stop() throws IOException {
        this.socket.close(); // Close the SocketChannel
    }

    // Class usage
    private static void printUsage() {
        System.out.println("usage: java Connection <PORT> [HOST]");
    }

    // Static method to send a message through a SocketChannel
    public static void send(SocketChannel socket, String message) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);      // Create a ByteBuffer with a capacity of 1024 bytes
        buffer.clear();                                     // Clear the buffer
        buffer.put(message.getBytes());                     // Put the message into the buffer
        buffer.flip();                                      // Flip the buffer to prepare it for writing
        while (buffer.hasRemaining()) {                     // Write the buffer to the socket
            socket.write(buffer);
        }
    }

    // Static method to receive a message from a SocketChannel
    public static String receive(SocketChannel socket) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);          // Create a ByteBuffer with a capacity of 1024 bytes
        int bytesRead = socket.read(buffer);                    // Read from the socket into the buffer
        return new String(buffer.array(), 0, bytesRead); // Convert the bytes in the buffer to a String and return it
    }

    public String readToken(String filename) {

        if (filename == null || filename.equals("")) {
            return null;
        }

        File file = new File(this.TOKEN_PATH + filename);

        // Check if the file exists
        if (!file.exists()) {
            System.out.println("File - "+ filename  + " - does not exist");
            return null;
        }

        // Retrieve file content
        StringBuilder fileContent = new StringBuilder(); // Create StringBuilder to hold file content
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file)); // Create BufferedReader to read file
            String line;
            while ((line = reader.readLine()) != null) {    // Read each line of the file
                fileContent.append(line);
            }
            reader.close(); // Close the BufferedReader
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent.toString(); // Return the file content as a String
    }

    public void writeToken(String filename, String content) {

        try {
            // Check if the file exists
            File file = new File(this.TOKEN_PATH + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            // Update file content
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bufferedWriter.write(content); // Write the content to the file
            bufferedWriter.close(); // Close the BufferedWriter

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate() throws Exception {

        String[] serverAnswer;
        String requestType;
        boolean takenUsername = false;
        boolean invalidCredentials = false;

        do {
            serverAnswer = Connection.receive(this.socket).split("\n");
            requestType = serverAnswer[0].toUpperCase();

            switch (requestType) {
                case "OPT" -> { // Option request
                    String menu = String.join("\n", Arrays.copyOfRange(serverAnswer, 1, serverAnswer.length));
                    System.out.println(menu);
                    Connection.send(this.socket, this.mainMenuGUI());
                }
                case "USR" -> { // Data request: username or password

                    String[] credentials;

                    do {
                        credentials = this.loginAndRegister(invalidCredentials, takenUsername);
                    } while ((credentials[0].equals("") || credentials[1].equals("")) && !(credentials[2].equals("BACK")));


                    if (credentials[2].equals("BACK")) {
                        Connection.send(this.socket, credentials[2]);
                        takenUsername = false;
                        invalidCredentials = false;
                    }
                    else {
                        Connection.send(this.socket, credentials[0].toLowerCase());

                        serverAnswer = Connection.receive(this.socket).split("\n");
                        requestType = serverAnswer[0].toUpperCase(); // It is going to be PSW

                        if (!requestType.equals("FIN")) {
                            Connection.send(this.socket, credentials[1]);
                        }
                    }
                }
                case "TKN" -> { // Token request: session token value
                    System.out.println(serverAnswer[1]);
                    System.out.println("Token file name: ");
                    String token;

                    boolean invalidToken = false;

                    do {
                        token = this.getTokenFromGUI(invalidToken);
                        invalidToken = token == null;
                    } while (token == null);

                    System.out.println("Token: " + token);
                    Connection.send(this.socket, token == null ? "invalid" : token);
                }
                case "NACK" -> { // Handle an error in authentication
                    System.out.println(serverAnswer[1]);

                    if(serverAnswer[1].equals("Username already in use")) {
                        takenUsername = true;
                    } else if(serverAnswer[1].equals("Wrong username or password")) {
                        invalidCredentials = true;
                    }

                    // If we receive an error authenticating, we will try again the same option chosen before, unless it wasn't chosen yet
                    // We first need to send the dummy "ACK" to the server, so it can send us the option request again
                    Connection.send(this.socket, "ACK");

                    if (authenticationOption > 0) {
                        serverAnswer = Connection.receive(this.socket).split("\n");
                        requestType = serverAnswer[0].toUpperCase();

                        // Retry connection option
                        Connection.send(this.socket,Integer.toString(authenticationOption));
                    }
                }
                case "AUTH" -> { // Authentication success. Receive session token value
                    System.out.println("Success. Session token was received.");
                    Connection.send(this.socket, "ACK");
                    this.writeToken(serverAnswer[1], serverAnswer[2]);
                }
                case "FIN" -> System.out.println(serverAnswer[1]); // If the server brokes the connection
                default -> System.out.println("Unknown server request type :" + requestType);
            }
        } while (!requestType.equals("AUTH") && !requestType.equals("FIN"));
        return requestType.equals("AUTH"); // Authentication success?
    }

    public void listening() throws Exception {

        String[] serverAnswer;
        String requestType = "";
        long lastTime = System.currentTimeMillis();
        long currentTime;
        Selector selector = Selector.open();
        this.socket.configureBlocking(false);
        this.socket.register(selector, SelectionKey.OP_READ);

        do {

            int readyChannels = selector.select(TIMEOUT);
            if (readyChannels == 0) {
                currentTime = System.currentTimeMillis() - lastTime;
                if(currentTime > TIMEOUT) {
                    System.out.println("Server is not responding. Closing connection...");
                    break;
                }
                continue;
            } else {
                lastTime = System.currentTimeMillis();
                selector.selectedKeys().clear();
            }

            serverAnswer = Connection.receive(this.socket).split("\n");
            requestType = serverAnswer[0].toUpperCase();
            System.out.println("REQUEST TYPE: " + Arrays.toString(serverAnswer));

            switch (requestType) {
                case "QUEUE" -> {
                    Connection.send(this.socket, "ACK");
                    queueGUI(serverAnswer[1]);
                }
                case "FIN" -> {
                    Connection.send(this.socket, "ACK");
                }
                case "INFO", "TURN", "SCORE" -> { // Player turn. Let's send something to server.
                    gameGUI(serverAnswer, requestType);
                    Connection.send(this.socket, "ACK");
                }
                case "GAMEOVER" -> {
                    Connection.send(this.socket, gameOverGUI(serverAnswer[1]));
                }
                case "PING" -> {
                    ; // Doesn't expect an answer back
                }
                default -> System.out.println("Unknown server request type");
            }

        } while (!requestType.equals("FIN"));
        selector.close();

    }

    public void initGUI() {
        this.playerGui = new PlayerGUI(10000);
    }

    public String mainMenuGUI() {
        authenticationOption = Integer.parseInt(this.playerGui.mainMenu());
        return Integer.toString(authenticationOption);
    }

    public String[] loginAndRegister(boolean invalidCredentials, boolean takenUsername) {
        return this.playerGui.loginAndRegister(invalidCredentials, takenUsername);
    }

    public String getTokenFromGUI(boolean invalidToken) {
        String[] result = this.playerGui.reconnect(invalidToken);

        if(result[1].equals("BACK"))
            return result[1];
        else
            return readToken(result[0]);
    }

    public void queueGUI(String serverMessage) {
        this.playerGui.queue(serverMessage);
    }

    public void gameGUI(String[] serverMessages, String requestType) {
        switch (requestType) {
            case "INFO" -> this.playerGui.info();
            case "TURN" -> this.playerGui.turn();
            case "SCORE" -> this.playerGui.updateScore(serverMessages);
        }
    }

    public String gameOverGUI(String serverMessage) {
        return this.playerGui.gameOver(serverMessage);
    }

    public void closeGUI() {
        if (this.playerGui != null) this.playerGui.close();
    }

    public static void main(String[] args) {

        // Check if there are enough arguments
        if (args.length < 1) {
            Connection.printUsage();
            return;
        }
        Connection connection = null;

        // Parse port and host arguments and create a Connection object
        try {

            int port = Integer.parseInt(args[0]);
            String host = args.length == 2 ? args[1] : Connection.DEFAULT_HOST;
            connection = new Connection(port, host);
            connection.start();
            connection.initGUI();

            // Start the connection and authenticate
            if (connection.authenticate())
                connection.listening();
            else
                connection.stop();

            connection.closeGUI();

        } catch (UnknownHostException exception) {
            System.out.println("Host not found: " + exception.getMessage());

        } catch (Exception exception) {
            System.out.println("I/O error: " + exception.getMessage());

        } finally {
            if (connection != null) {
                try {
                    connection.stop();
                    connection.closeGUI();
                } catch (IOException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}