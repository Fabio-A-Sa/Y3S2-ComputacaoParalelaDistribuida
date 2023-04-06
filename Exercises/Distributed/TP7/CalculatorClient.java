import java.net.*;
import java.io.*;
 
public class CalculatorClient {

    private int port;
    private String hostname;
    private Socket socket;

    public CalculatorClient(int port, String host) {
        this.port = port;
        this.hostname = hostname;
    }

    public void start() throws IOException, UnknownHostException {
        this.socket = new Socket(this.hostname, this.port);
    }

    public void sendNumbers(String[] args) throws IOException {

        PrintWriter sender = new PrintWriter(this.socket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        for (int index = 2 ; index < args.length ; index++){
            System.out.println("Sending number: " + args[index]);
            sender.println(args[index]);
            System.out.println("Partial total: " + receiver.readLine());
        }

        System.out.println("something 1234");
        System.out.println("Server total sum: " + receiver.readLine());
    }
 
    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.out.println("usage: java CalculatorClient <HOST> <PORT> [...NUMBERS]");
            return;
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            CalculatorClient client = new CalculatorClient(port, hostname);
            client.start();
            client.sendNumbers(args);

        } catch (UnknownHostException exception) {
            System.out.println("Server not found: " + exception.getMessage());
            exception.printStackTrace();
 
        } catch (IOException exception) {
            System.out.println("I/O error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}