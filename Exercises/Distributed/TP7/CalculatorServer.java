import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class CalculatorServer {

    private int port;
    private int globalSum;
    private Map<String, Integer> clients;
    private ServerSocket serverSocket;
    private ReentrantLock lock;

    public CalculatorServer(int port) {
        this.port = port;
        this.globalSum = 0;
        this.clients = new HashMap<String, Integer>(); // (clientName, partialSum)
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

        int partialSum;
        this.lock.lock();
        if (this.clients.containsKey(clientName)) {
            partialSum = this.clients.get(clientName);
        } else {
            this.clients.put(clientName, 0);
            partialSum = 0;
        }
        this.lock.unlock();

        String clientInput;
        int clientNumber;
        while ((clientInput = receiver.readLine()) != null) {
            System.out.println(clientInput);
            clientNumber = Integer.parseInt(clientInput);
            partialSum += clientNumber;
            sender.println(partialSum);
        }

        this.lock.lock();
        this.clients.put(clientName, partialSum);
        int totalSum = 0;
        for (int value : this.clients.values()) {
            totalSum += value;
        }
        sender.println(totalSum);
        this.lock.unlock();
    }
 
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("usage: java CalculatorServer <PORT>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        try {
            CalculatorServer server = new CalculatorServer(port);
            server.start();
            server.run();

        } catch (IOException exception) {
            System.out.println("Server exception: " + exception.getMessage());
            exception.printStackTrace();
        }  
    }
}