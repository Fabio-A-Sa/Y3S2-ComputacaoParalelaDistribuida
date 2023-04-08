import java.net.*;
import java.io.*;

public class Connection {
    
    private int port;
    private Socket socket;
    private String token;
    private PrintWriter sender;
    private BufferedReader receiver;
    private final String host = "localhost";

    public Connection(int port) {
        this.port = port;
    }

    public void start() throws IOException, UnknownHostException {
        this.socket = new Socket(this.host, this.port);
        this.sender = new PrintWriter(this.socket.getOutputStream(), true);
        this.receiver = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void stop() throws IOException {
        this.socket.close();
    }

    public boolean autenticate(String[] args) throws IOException {

        // Send autentication arguments
        for (int index = 1 ; index < args.length ; index++)
            sender.println(args[index]);
        
        // Register e Login implícito ou Register + Login?
        switch (receiver.readLine()) {
            case "ACK":
                this.token = receiver.readLine();
                System.out.println("Success. Your session token is: " + this.token);
                return true;
            case "NACK":
                String error = receiver.readLine();
                System.out.println("Error: " + error);
                return false;
            default:
                System.out.println("Error: unknown server answer");
                return false;
        }
    }

    public void listening() throws IOException, UnknownHostException {
        System.out.println("TODO: client-server interaction, if autentication sucessfully");
    }

    private static void printUsage() {
        System.out.println("usage: java Connection <PORT> -login <USERNAME> <PASSWORD>");
        System.out.println("                              -register <USERNAME> <PASSWORD>");
        System.out.println("                              -reconnect <TOKEN>");
    }
 
    public static void main(String[] args) {

        if ( args.length < 3 ||
            (!(args[1].equals("-login") && args.length == 4) &&
             !(args[1].equals("-register") && args.length == 4)) &&
             !(args[1].equals("-reconnect") && args.length == 3)) {
            System.out.println("unknown command");
            Connection.printUsage();
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            Connection connection = new Connection(port);
            connection.start();
            if (connection.autenticate(args))
                connection.listening();
            else 
                connection.stop();

        } catch (UnknownHostException exception) {
            System.out.println("Server not found: " + exception.getMessage());
 
        } catch (IOException exception) {
            System.out.println("I/O error: " + exception.getMessage());
        }
    }
}
