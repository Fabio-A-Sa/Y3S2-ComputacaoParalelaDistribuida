import java.net.*;
import java.io.*;

public class Connection {
    
    private final String host = "localhost";
    private int port;
    private Socket socket;
    private String token;
    private PrintWriter sender;
    private BufferedReader receiver;

    public Connection(int port) {
        this.port = port;
    }

    public void start() throws IOException, UnknownHostException {
        this.socket = new Socket(this.host, this.port);
        this.sender = new PrintWriter(this.socket.getOutputStream(), true);
        this.receiver = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void autenticate(String[] args) throws IOException {

        // Envia os arguments
        // Se correr bem, recebe o token de sessão
        for (int index = 1 ; index < args.length ; index++) {
            System.out.println("Sending argument: " + args[index]); // para retirar, claro
            sender.println(args[index]);
        }
        
        // register e login implícito ou register + login
        // lidar com falhas aqui, a resposta não pode ser o token
        System.out.println("Session token " + receiver.readLine());
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

        // melhorar este passo: deixar exatamente o número de argumentos do modo, senão dá asneira para o servidor
        if (args.length < 3 || args.length > 4 || !(args[1].equals("-login") || 
                        args[1].equals("-register") || args[1].equals("-reconnect"))) {
            System.out.println("unknown command");
            Connection.printUsage();
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            Connection connection = new Connection(port);
            connection.start();
            connection.autenticate(args);
            connection.listening();

        } catch (UnknownHostException exception) {
            System.out.println("Server not found: " + exception.getMessage());
 
        } catch (IOException exception) {
            System.out.println("I/O error: " + exception.getMessage());
        }
    }
}
