import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private int port;
    private ServerSocket serverSocket;
    private ReentrantLock lock;
    private List<Client> clients;

    public Server(int port) {
        this.port = port;
        this.lock = new ReentrantLock();
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        System.out.println("Server is listening on port " + this.port);
    }

    public void run() throws IOException {
        while (true) {
            Socket clientSocket = this.serverSocket.accept();
            System.out.println("Client connected: " +  clientSocket.getPort());
            Thread clientThread = new Thread(() -> {
                try {
                    this.handleClient(clientSocket);
                } catch (IOException exception) {
                    System.out.println("Error handling client: " + exception.getMessage());
                }
            });
            clientThread.start();
        }
    }

    public void login(PrintWriter sender, BufferedReader receiver, Socket clientSocket) throws IOException { 
        System.out.println("Login logic");
    }

    public void register(PrintWriter sender, BufferedReader receiver, Socket clientSocket) throws IOException { 
        System.out.println("Register logic");
    }

    public void reconnect(PrintWriter sender, BufferedReader receiver, Socket clientSocket) throws IOException { 
        System.out.println("Reconnect logic");
    }

    public void handleClient(Socket clientSocket) throws IOException {
        
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        switch(receiver.readLine()) {
            case "-login":
                this.login(sender, receiver, clientSocket);
                break;
            case "-register":
                this.register(sender, receiver, clientSocket);
                break;
            case "-reconnect":
                this.reconnect(sender, receiver, clientSocket);
                break;
            default:
                sender.println("error in key");
                clientSocket.close();
        }
    }
 
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("usage: java Server <PORT>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        try {
            Server server = new Server(port);
            server.start();
            server.run();

        } catch (IOException exception) {
            System.out.println("Server exception: " + exception.getMessage());
        }  
    }
}