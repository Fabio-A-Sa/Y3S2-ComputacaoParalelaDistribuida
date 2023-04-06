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

    public void send() throws IOException, UnknownHostException {
        System.out.println("Sending Calculator");
        /**
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("new Date()?".toString());

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            String time = reader.readLine();
 
            System.out.println(time);
         */
    }
 
    public static void main(String[] args) {
        
        if (args.length != 2) {
            System.out.println("usage: java CalculatorClient <HOST> <PORT> [NUMBERS]");
            return;
        }
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            CalculatorClient client = new CalculatorClient(hostname, port);
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