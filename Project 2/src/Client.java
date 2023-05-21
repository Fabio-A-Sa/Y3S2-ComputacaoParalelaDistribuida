import java.nio.channels.SocketChannel;

public class Client {

    private final String username;
    private final String password;
    private final String token;
    private Long rank;
    private SocketChannel socket;
    private int timeInQueue = 0;

    Client(String username, String password, String token, Long rank, SocketChannel socket) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.rank = rank;
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public Long getRank() {
        return this.rank;
    }

    public void incrementRank(int value) {
        this.rank += value;
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public boolean equals(Client client) {
        return this.username.equals(client.getUsername());
    }
}