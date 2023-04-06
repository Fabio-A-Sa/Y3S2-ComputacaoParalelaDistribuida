import java.io.*;
import java.net.*;
import java.util.*;
 
public class CalculatorServer {

    private final int port;
    private int globalSum;
    private Map<Integer, Integer> partialSum;

    CalculatorServer(int port) {
        this.port = port;
        this.globalSum = 0;
        this.partialSum = new HashMap<Integer, Integer>();
    }
 
    public static void main(String[] args) {
        
        if (args.length < 1) return;

 
        int port = Integer.parseInt(args[0]);

        CalculatorServer server = new CalculatorServer(port);
 
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
    }
}