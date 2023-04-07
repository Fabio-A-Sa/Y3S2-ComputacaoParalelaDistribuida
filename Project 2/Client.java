import java.net.*;
import java.io.*;
 
public class Client {

    private String username;
    private String password;
    private String token;
    private int rank;

    Client(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.rank = 0;
    }

    public String getUsername() { 
        return this.username; 
    }

    public String getPassword() { 
        return this.password; 
    }

    public String getToken() { 
        return this.token; 
    }

    public void incrementRank(int value) {
        this.rank += value;
    }
}