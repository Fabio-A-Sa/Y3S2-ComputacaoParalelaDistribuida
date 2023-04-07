import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private int port;
    private ServerSocket serverSocket;
    private ReentrantLock lock;

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

    public void handleClient(Socket clientSocket) throws IOException {
        
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String clientName = clientSocket.getInetAddress().getHostName();
        System.out.println(clientName);
        String clientInput;
        while ((clientInput = receiver.readLine()) != null) {
            System.out.println(clientInput);
        }

        sender.println("Autentication sucessfully");
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