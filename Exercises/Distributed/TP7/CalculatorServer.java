import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class CalculatorServer {

    private int port;
    private int globalSum;
    private Map<Integer, Integer> partialSum;
    private ServerSocket serverSocket;
    private ReentrantLock lock;

    public CalculatorServer(int port) {
        this.port = port;
        this.globalSum = 0;
        this.partialSum = new HashMap<Integer, Integer>();
        this.lock = new ReentrantLock();
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        System.out.println("Server is listening on port " + this.port);
    }

    public void run() throws IOException {
        //TODO: while true
        System.out.println("running");
    }
 
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("Missing argument: server port");
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

        /*
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
        
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
 
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
                String time = reader.readLine();

                System.out.println("New client connected: "+ time);
 
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
 
                writer.println(new Date().toString());
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        */
    }
}