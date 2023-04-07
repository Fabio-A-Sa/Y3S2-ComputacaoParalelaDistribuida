import java.net.*;
import java.io.*;

public class Connection {
    
    private final String host = "localhost";
    private int port;
    private Socket socket;

    public Connection(int port) {
        this.port = port;
    }

    public void start() throws IOException, UnknownHostException {
        this.socket = new Socket(this.host, this.port);
    }

    public void sendArgs(String[] args) throws IOException {

        PrintWriter sender = new PrintWriter(this.socket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        for (int index = 2 ; index < args.length ; index++){
            System.out.println("Sending number: " + args[index]);
            sender.println(args[index]);
            System.out.println("Partial total: " + receiver.readLine());
        }

        System.out.println("Server total sum: " + receiver.readLine());
    }

    private static void printUsage() {
        System.out.println("usage: java Connection <PORT> -login <USERNAME> <PASSWORD>");
        System.out.println("                              -register <USERNAME> <PASSWORD>");
        System.out.println("                              -reconnect <TOKEN>");
    }
 
    public static void main(String[] args) {

        if (args.length < 3) {
            Connection.printUsage();
            return;
        }

        try {

            switch(args[1]) {
                case "-login":
                    break;
                case "-register":
                    break;
                case "-reconnect":
                    break;
                default:
                    System.out.println("unknown command");
                    Connection.printUsage();
                    return;
            }

            int port = Integer.parseInt(args[0]);
            Connection connection = new Connection(port);
            connection.start();
            connection.sendArgs(args);

        } catch (UnknownHostException exception) {
            System.out.println("Server not found: " + exception.getMessage());
 
        } catch (IOException exception) {
            System.out.println("I/O error: " + exception.getMessage());
        }
    }
}
