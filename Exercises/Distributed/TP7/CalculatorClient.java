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
        System.out.println("Starting Calculator");
    }

    public void send(String[] args) throws IOException, UnknownHostException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            String serverResponse = in.readLine();
            System.out.println("Server response: " + serverResponse);
        }
        System.out.println("Final sum: " + in.readLine());
    }
 
    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.out.println("usage: java CalculatorClient <HOST> <PORT> [NUMBERS]");
            return;
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            CalculatorClient client = new CalculatorClient(port, hostname);
            client.start();
            client.send();

        } catch (UnknownHostException exception) {
            System.out.println("Server not found: " + exception.getMessage());
            exception.printStackTrace();
 
        } catch (IOException exception) {
            System.out.println("I/O error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}