import java.util.List;
import java.net.Socket;

public class Game {

    private int players;
    private List<Socket> userSockets;

    public Game(int players, List<Socket> userSockets) {
        this.userSockets = userSockets;
        this.players = players;
    }

    public void start() {
        System.out.println("Starting game with " + userSockets.size() + " players");
    } 
}