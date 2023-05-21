import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.simple.parser.ParseException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class Server {

    // Server
    private final int port;
    private final int mode;
    private ServerSocketChannel serverSocket;
    private final ExecutorService threadPoolGame;
    private final ExecutorService threadPoolAuth;
    private int time;
    private long startTime;
    private final ReentrantLock time_lock;

    // Timeouts
    private final int TIMEOUT = 30000;          // Timeout to avoid slow clients in authentication (milliseconds)
    private final int PING_INTERVAL = 10000;    // Time between pings to clients (milliseconds)
    private long lastPing;

    // Game
    private final int MAX_CONCURRENT_GAMES = 5;
    private final int PLAYERS_PER_GAME = 2;

    // Represents the time, in seconds, for the server to increase the tolerated interval
    // between players with different rankings
    private final int TIME_FACTOR = 1;

    // Database
    private Database database;
    private ReentrantLock database_lock;
    private final String DATABASE_PATH = "Server/databases/";

    // Clients
    private List<Client> waiting_queue;
    private ReentrantLock waiting_queue_lock;
    private final int MAX_CONCURRENT_AUTH = 5;

    // Token Generation
    private int token_index;
    private ReentrantLock token_lock;

    // GUI
    private final ServerGUI serverGUI;

    public Server(int port, int mode, String filename) throws IOException, ParseException {

        // Server information
        this.port = port;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();

        // Concurrent fields
        this.threadPoolGame = Executors.newFixedThreadPool(this.MAX_CONCURRENT_GAMES);
        this.threadPoolAuth = Executors.newFixedThreadPool(this.MAX_CONCURRENT_AUTH);
        this.waiting_queue = new ArrayList<Client>();
        this.database = new Database(this.DATABASE_PATH + filename);
        this.token_index = 0;
        this.time = 0;

        // Locks
        this.waiting_queue_lock = new ReentrantLock();
        this.database_lock = new ReentrantLock();
        this.token_lock = new ReentrantLock();
        this.time_lock = new ReentrantLock();

        // Server GUI
        this.serverGUI = new ServerGUI();
        this.lastPing = System.currentTimeMillis();
    }

    // Server usage
    public static void printUsage() {
        System.out.println("usage: java Server <PORT> <MODE> <DATABASE>");
        System.out.println("       <MODE>");
        System.out.println("           0 - Simple Mode");
        System.out.println("           1 - Ranking Mode");
        System.out.println("       <DATABASE>");
        System.out.println("           JSON file name inside Server/databases folder");
    }

    // Starts the server and listens for connections on the specified port
    public void start() throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(this.port));
        System.out.println("Server is listening on port " + this.port + " with " + (this.mode == 1 ? "rank" : "simple") + " mode");
    }

    // Updates the server time by computing the elapsed time since the server started
    private void updateServerTime() {
        this.time_lock.lock();
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        this.time = (int) (elapsedTime / 1000);
        this.time_lock.unlock();
    }

    // Resets the server time to 0 and updates the start time
    private void resetServerTime() {
        this.time_lock.lock();
        this.startTime = System.currentTimeMillis();
        this.time = 0;
        this.time_lock.unlock();
    }

    // Schedule games by creating a new game with players from the waiting queue - simple mode
    private void gameSchedulerSimple() {

        this.waiting_queue_lock.lock();

        if (this.waiting_queue.size() >= this.PLAYERS_PER_GAME) { // Check if there are enough players in the waiting queue
            List<Client> gameClients = new ArrayList<>();
            for (int i = 0; i < this.PLAYERS_PER_GAME; i++) {
                gameClients.add(this.waiting_queue.remove(0)); // Remove players from the waiting queue and add them to the game
                System.out.println("Client " + gameClients.get(i).getUsername() + " removed from waiting queue");
            }
            Runnable gameRunnable = new Game(gameClients, this.database, this.database_lock, this.waiting_queue, this.waiting_queue_lock);

            this.threadPoolGame.execute(gameRunnable); // Execute the game on a thread from the thread pool
        }
        serverStatusGUI();

        this.waiting_queue_lock.unlock();
    }

    private void gameSchedulerRank() {

        this.waiting_queue_lock.lock();
        if (this.waiting_queue.size() >= this.PLAYERS_PER_GAME) { // Check if there are enough clients in the queue to start a game
            this.updateServerTime();        // Update the server time
            this.sortClients();             // Sort the clients by rank
            int slack = this.getSlack();    // Get the maximum allowable difference in rank between players in a game
            for (int i = 0 ; i < this.waiting_queue.size() - this.PLAYERS_PER_GAME + 1; i++) {

                // Check if the difference in rank between the clients is within the slack limit
                Client first = this.waiting_queue.get(i + this.PLAYERS_PER_GAME - 1);
                Client second = this.waiting_queue.get(i);

                if (first.getRank() - second.getRank() > slack) {
                    continue; // If not, skip to the next pair of clients
                }

                // Remove the clients from the waiting queue and add them to the game
                List<Client> gameClients = new ArrayList<>();
                for (int j = 0; j < this.PLAYERS_PER_GAME; j++) {
                    gameClients.add(this.waiting_queue.remove(i));
                }

                // Create a new Game instance and execute it in a new thread
                Runnable gameRunnable = new Game(gameClients, this.database, this.database_lock, this.waiting_queue, this.waiting_queue_lock);
                this.threadPoolGame.execute(gameRunnable);
                this.waiting_queue_lock.unlock();
                this.resetServerTime();
                serverStatusGUI();
                return;
            }
        }
        serverStatusGUI();

        this.waiting_queue_lock.unlock();
    }

    // Method to calculate the current slack time based on the server time and the time factor
    private int getSlack() {
        this.time_lock.lock();
        int current_time = this.time;
        this.time_lock.unlock();
        return current_time / this.TIME_FACTOR;
    }

    // Handle incoming client connections
    private void connectionAuthenticator() {
        while (true) {
            try {
                SocketChannel clientSocket = this.serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteAddress());

                Runnable newClientRunnable = () -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception exception) {
                        System.out.println("Error handling client: " + exception);
                    }
                };
                this.threadPoolAuth.execute(newClientRunnable);

            } catch (Exception exception) {
                System.out.println("Error handling client: " + exception);
            }
        }
    }

    private void pingClients() {
        if(System.currentTimeMillis() - this.lastPing > this.PING_INTERVAL) {
            this.lastPing = System.currentTimeMillis();

            this.waiting_queue_lock.lock();
            if (this.waiting_queue.size() == 0) {
                this.waiting_queue_lock.unlock();
                return;
            }

            System.out.println("Pinging clients...");

            Iterator<Client> iterator = this.waiting_queue.iterator();
            while (iterator.hasNext()) {
                Client client = iterator.next();
                try {
                    Server.request(client.getSocket(), "PING", "");
                } catch (IOException exception) {
                    System.out.println("Error pinging client: " + exception);
                    iterator.remove();
                } catch (Exception e) {
                    this.waiting_queue_lock.unlock();
                    throw new RuntimeException(e);
                }
            }
            this.waiting_queue_lock.unlock();
        }
    }

    public void run() throws IOException {

        // Keeps an eye on the waiting list and launches a new game whenever it can, according to the threadPoll
        Thread gameSchedulerThread = new Thread(() -> {
            while (true) {
                pingClients();
                if (mode == 0)
                    gameSchedulerSimple();
                else
                    gameSchedulerRank();
            }
        });

        // Authenticates all connections and push new clients into waiting list
        Thread connectionAuthenticatorThread = new Thread(() -> {
            while (true) connectionAuthenticator();
        });

        // Resets the saved player tokens before starting the server
        this.database_lock.lock();
        this.database.resetTokens();
        this.database_lock.unlock();

        // Run threads
        gameSchedulerThread.start();
        connectionAuthenticatorThread.start();
    }

    // This method generates a session token using a unique server index and the username
    // For security, token will be hashed using BCrypt
    private String getToken(String username) {
        this.token_lock.lock();
        int index = this.token_index;
        this.token_index++;
        this.token_lock.unlock();
        return BCrypt.hashpw(username + index, BCrypt.gensalt());
    }

    // Inserts a client in the waiting queue
    private void insertClient(Client client) {

        try {
            this.waiting_queue_lock.lock();
            for (Client c : this.waiting_queue) {
                if (c.equals(client)) {
                    // If the client is already in the queue, their socket is updated with the new one
                    c.setSocket(client.getSocket());
                    System.out.println("Client " + client.getUsername() + " reconnected. Queue size: " + this.waiting_queue.size());
                    Server.request(client.getSocket(), "QUEUE", "You are already in the waiting queue with " + client.getRank() + " points.");
                    Connection.receive(client.getSocket());
                    this.waiting_queue_lock.unlock();
                    return;
                }
            }

            // If the client is not already in the queue, add them to the end of the queue
            this.waiting_queue.add(client);
            Server.request(client.getSocket(), "QUEUE", "You entered in waiting queue with ranking  " + client.getRank() + " points.");
            Connection.receive(client.getSocket());
            System.out.println("Client " + client.getUsername() + " is now in waiting queue. Queue size: " + this.waiting_queue.size());

        } catch (Exception exception) {
            System.out.println("Error during insert in waiting queue. Info: " + exception.getMessage());
        } finally {
            this.waiting_queue_lock.unlock();
        }
    }

    // Sorts the waiting queue based on the clients' ranks
    private void sortClients() {
        this.waiting_queue_lock.lock();
        this.waiting_queue.sort(Comparator.comparingLong(Client::getRank));
        this.waiting_queue_lock.unlock();
    }

    public Client login(SocketChannel clientSocket, String username, String password) throws Exception {

        if (Objects.equals(username, "BACK") || Objects.equals(password, "BACK"))
            return null;

        String token = this.getToken(username);
        Client client;

        try {
            this.database_lock.lock();
            client = this.database.login(username, password, token, clientSocket);
            this.database.backup();
            this.database_lock.unlock();

            if (client != null) {
                Server.request(clientSocket, "AUTH", "token-" + username + ".txt\n" + token);
                Connection.receive(clientSocket);
                return client;
            } else {
                Server.request(clientSocket, "NACK", "Wrong username or password");
                Connection.receive(clientSocket);
            }

        } catch (Exception e) {
            Server.request(clientSocket, "NACK", e.getMessage());
            Connection.receive(clientSocket);
        }
        return null;
    }

    public Client register(SocketChannel clientSocket, String username, String password) throws Exception {

        if (Objects.equals(username, "BACK") || Objects.equals(password, "BACK"))
            return null;

        String token = this.getToken(username);
        Client client;

        try {
            this.database_lock.lock();
            client = this.database.register(username, password, token, clientSocket);
            this.database.backup();
            this.database_lock.unlock();

            if (client != null) {
                Server.request(clientSocket, "AUTH", "token-" + username + ".txt\n" + token);
                Connection.receive(clientSocket);
                return client;
            } else {
                Server.request(clientSocket, "NACK", "Username already in use");
                Connection.receive(clientSocket);
            }

        } catch (Exception e) {
            Server.request(clientSocket, "NACK", e.getMessage());
            Connection.receive(clientSocket);
        }
        return null;
    }

    public Client reconnect(SocketChannel clientSocket, String token) throws Exception {

        this.database_lock.lock();
        Client client = this.database.reconnect(token, clientSocket);
        this.database.backup();
        this.database_lock.unlock();

        if (client != null) {
            Server.request(clientSocket, "AUTH", "token-" + client.getUsername() + ".txt\n" + token);
            Connection.receive(clientSocket);
        } else {
            Server.request(clientSocket, "NACK","Invalid session token");
            Connection.receive(clientSocket);
        }
        return client;
    }

    // FIN + Error Message > receives ACK for client acknowledgement
    // TKN + Message > receives a session token value
    // USR + Message > receives a username
    // PSW + Message > receives a password
    // OPT + Menu > receives an option
    // AUTH + tokenName + tokenName > receives ACK for client acknowledgement
    // INFO + Message > receives ACK for client acknowledgement
    // NACK + Error > receives ACK for client acknowledgement
    // TURN + Message > receives any input
    public static void request(SocketChannel socket, String requestType, String message) throws Exception {
        Connection.send(socket, requestType + "\n" + message);
    }

    // Deal with new connection
    public void handleClient(SocketChannel clientSocket) throws Exception {

        String input;
        Client client = null;
        long startTime = System.currentTimeMillis();

        do {

            // Check if timeout has been reached
            if (System.currentTimeMillis() - startTime >= this.TIMEOUT) {
                System.out.println("Connection timeout");
                Server.request(clientSocket, "FIN", "Connection terminated");
                return;
            }

            // Login, register, reconnect and quit choosing options
            Server.request(clientSocket, "OPT", "1 - Login\n2 - Register\n3 - Reconnect\n4 - Quit");
            input = Connection.receive(clientSocket).toUpperCase();

            // Quit option -> close connection
            if (input.equals("4")) {
                Server.request(clientSocket, "FIN", "Connection terminated");
                clientSocket.close();
                return;
            }

            // Unknown option. Refuse option and try again.
            if (!(input.equals("1") || input.equals("2") || input.equals("3"))) {
                Server.request(clientSocket, "NACK", "Option refused");
                Connection.receive(clientSocket);
                continue;
            }

            // Authentication protocol
            String username, password, token;
            switch (input) {
                case "1" -> {
                    Server.request(clientSocket, "USR", "Username?");
                    username = Connection.receive(clientSocket);
                    System.out.println(username);
                    if (username.equals("BACK")) continue;
                    Server.request(clientSocket, "PSW", "Password?");
                    password = Connection.receive(clientSocket);
                    client = this.login(clientSocket, username, password);
                }
                case "2" -> {
                    Server.request(clientSocket, "USR", "Username?");
                    username = Connection.receive(clientSocket);
                    if (username.equals("BACK")) continue;
                    Server.request(clientSocket, "PSW", "Password?");
                    password = Connection.receive(clientSocket);
                    client = this.register(clientSocket, username, password);
                }
                case "3" -> {
                    Server.request(clientSocket, "TKN", "Token?");
                    token = Connection.receive(clientSocket);
                    System.out.println("TOKEN: " + token);
                    if (token.equals("BACK")) continue;
                    client = this.reconnect(clientSocket, token);
                }
                default -> {
                    Server.request(clientSocket, "FIN", "Connection terminated");
                    clientSocket.close();
                    return;
                }
            }

            // Deal with waiting queue
            if (client != null) {
                this.insertClient(client);
                if (this.mode == 1) {
                    this.sortClients();
                    this.resetServerTime();
                }
            }

        } while (client == null);
        serverStatusGUI();
    }

    public void serverStatusGUI() {
        int total_games = ((ThreadPoolExecutor) threadPoolGame).getActiveCount();
        this.waiting_queue_lock.lock();
        String[] waiting_queue = new String[this.waiting_queue.size()];
        for (int i = 0; i < this.waiting_queue.size() && i < 5; i++) {
            waiting_queue[i] = this.waiting_queue.get(i).getUsername();
        }
        serverGUI.setQueue(String.valueOf(this.waiting_queue.size()), waiting_queue);
        this.waiting_queue_lock.unlock();
        serverGUI.setGames(String.valueOf(total_games));

        this.database_lock.lock();
        serverGUI.setLeaderboard(this.database.getLeaderboard());
        this.database_lock.unlock();
    }

    public static void main(String[] args) {

        // Check if there are enough arguments
        if (args.length != 3) {
            Server.printUsage();
            return;
        }

        // Parse port and host arguments and create a Server object
        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);
        String filename = args[2];
        if (mode != 0 && mode != 1) {
            Server.printUsage();
            return;
        }

        // Start the connection
        try {
            Server server = new Server(port, mode, filename);
            server.start();
            server.run();
        } catch (IOException | ParseException exception) {
            System.out.println("Server exception: " + exception.getMessage());
        }
    }
}