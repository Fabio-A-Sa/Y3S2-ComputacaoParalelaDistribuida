import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game implements Runnable {

    private final List<Client> players;
    private final Database database;
    private final ReentrantLock database_lock;
    private final List<Client> waiting_queue;
    private final ReentrantLock waiting_queue_lock;
    private final int ROUNDS = 2;

    public Game(List<Client> players, Database database, ReentrantLock database_lock,
                List<Client> waiting_queue,
                ReentrantLock waiting_queue_lock) {
        this.players = players;
        this.database = database;
        this.database_lock = database_lock;
        this.waiting_queue = waiting_queue;
        this.waiting_queue_lock = waiting_queue_lock;
    }

    public void run() {
        try {
            System.out.println("Starting game with " + this.players.size() + " players");
            String winner = this.rounds();
            System.out.println("Game finished. Winner: " + winner);
            this.askPlayAgain(winner);
        } catch (Exception exception) {
            System.out.println("Exception ocurred during game. Connection close. : " + exception.getMessage());
            this.notifyPlayers("FIN", "Exception ocurred during game. Connection close.", null);
        }
    }

    private SocketChannel[] getPlayersSockets() {
        SocketChannel[] sockets = new SocketChannel[this.players.size()];
        for (int i = 0 ; i < this.players.size() ; i++) {
            sockets[i] = this.players.get(i).getSocket();
        }
        return sockets;
    }

    private void askPlayAgain(String winner) throws Exception {
        for (Client player : this.players) {

            Server.request(player.getSocket(), "GAMEOVER", winner);
            String answer = Connection.receive(player.getSocket());

            // Wants to play more: is placed in the waiting queue
            if (answer.equals("Y")) {
                insertInQueue(player);

                // Don't want to play again: session token is invalidated and connection is closed
            } else {
                Server.request(player.getSocket(), "FIN", "Connection close");
                this.database_lock.lock();
                this.database.invalidateToken(player);
                this.database.backup();
                this.database_lock.unlock();
                player.getSocket().close();
            }
        }
    }

    private void insertInQueue(Client player) {
        this.waiting_queue_lock.lock();
        try {
            for (Client c : this.waiting_queue) {
                //System.out.println("Comparing " + c.getUsername() + " with " + player.getUsername());
                if (c.equals(player)) {
                    // If the client is already in the queue, their socket is updated with the new one
                    System.out.println("FOUND DUPLICATE IN QUEUE");
                    c.setSocket(player.getSocket());
                    System.out.println("Client " + player.getUsername() + " reconnected. Queue size: " + this.waiting_queue.size());
                    Server.request(player.getSocket(), "QUEUE", "You entered in waiting queue with ranking  " + player.getRank() + " points.");
                    Connection.receive(player.getSocket());
                    this.waiting_queue_lock.unlock();
                    return;
                }
            }

            // If the client is not already in the queue, add them to the end of the queue

            this.waiting_queue.add(player);
            Server.request(player.getSocket(), "QUEUE", "You entered in waiting queue with ranking  " + player.getRank() + " points.");
            Connection.receive(player.getSocket());
            System.out.println("Client " + player.getUsername() + " is now in waiting queue. Queue size: " + this.waiting_queue.size());
            this.waiting_queue_lock.unlock();

        } catch (Exception exception) {
            System.out.println("Error during insert in waiting queue. Info: " + exception.getMessage());
        }
    }

    /*
     * The game will ask each user to throw 2 dices.
     * The game is made of at least 2 players.
     * Each user will throw the dices simultaneously.
     * The server will give the result of the throw to each user.
     * Wins the player that has the biggest score after N rounds.
     * If there's a tie, the players will divide between themselfs the gained elo.
     */
    private String rounds() throws Exception {

        // Game started
        this.notifyPlayers("INFO", "Game Started", null);

        if(this.players.size() < 2) {
            this.notifyPlayers("FIN", "Not enough players to start the game", null);
            return "Not enough players to start the game";
        }

        // Playing
        int[] dices = new int[this.players.size()];
        for (int round = 0 ; round < this.ROUNDS ; round++) {
            for (int i = 0; i < this.players.size(); i++) {
                currentResults(dices, round);
                Client player = this.players.get(i);
                this.notifyPlayers("INFO", "It's " + player.getUsername() + " turn to throw the dice", player);
                Server.request(player.getSocket(), "TURN", "Your turn to throw the dice. Press any character to continue");
                System.out.println(" round " + round + " - " + Connection.receive(player.getSocket()) + ";");
                dices[i] += this.throwDices();
            }
        }

        String winner = "";
        int winnerScore = 0;

        // Sending results
        for (int i = 0 ; i < this.players.size() ; i++) {
            Client player = this.players.get(i);
            this.notifyPlayers("INFO", "Player " + player.getUsername() + " have " + dices[i] + " points", null);
            player.incrementRank(dices[i]);
            this.database_lock.lock();
                this.database.updateRank(player, dices[i]);
                this.database.backup();
            this.database_lock.unlock();

            if (dices[i] > winnerScore) {
                winner = player.getUsername() + " won with " + dices[i] + " points!";
                winnerScore = dices[i];
            }
        }

        return winner;
    }

    private void currentResults(int[] dices, int currentRound) {
        StringBuilder results = new StringBuilder();
        results.append("Round: ").append(currentRound + 1).append("\n");
        int i = 0;
        for (Client player : this.players) {
            results.append(player.getUsername()).append(" Score: ").append(dices[i++]).append("\n");
        }
        this.notifyPlayers("SCORE", results.toString(), null);
    }

    private void notifyPlayers(String messageType, String message, Client excluded) {
        try {
            for (Client client : this.players) {
                if (excluded != null && client.equals(excluded)) continue;
                Server.request(client.getSocket(), messageType, message);
                Connection.receive(client.getSocket());
            }
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getMessage());
        }
    }

    private int throwDices() {
        Random rand = new Random();
        return rand.nextInt(12) + 1;
    }
}