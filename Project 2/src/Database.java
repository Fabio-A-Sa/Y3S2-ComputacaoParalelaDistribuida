import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.crypto.bcrypt.BCrypt;

class Database {

    private final File file;
    private final JSONObject database;

    public Database(String filename) throws IOException, ParseException {

        // File verification
        this.file = new File(filename);
        if (!file.exists()) {
            createEmptyFile();
        }

        // File content
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        // Database as JSON Object
        this.database = (JSONObject) new JSONParser().parse(sb.toString());
    }

    // Creates an empty file with an empty JSON object, containing an empty array with the key "database
    private void createEmptyFile() throws IOException {
        JSONObject emptyObject = new JSONObject();
        emptyObject.put("database", new JSONArray());
        FileWriter writer = new FileWriter(this.file);
        writer.write(emptyObject.toJSONString());
        writer.close();
    }

    // Writes the current database to the file
    public void backup() throws IOException {
        FileWriter writer = new FileWriter(this.file);
        writer.write(this.database.toJSONString());
        writer.close();
    }

    // Login method to authenticate a user based on their username and password
    public Client login(String username, String password, String token, SocketChannel socket) {

        // Get the array of users from the database
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            String storedUsername = (String) user.get("username");
            String storedPassword = (String) user.get("password");

            // If a match is found, update the user's token and return a new Client object
            if (storedUsername.equals(username) && BCrypt.checkpw(password, storedPassword)) {
                user.put("token", token);
                Long rank = ((Number) user.get("rank")).longValue();
                return new Client(username, storedPassword, token, rank, socket);
            }
        }
        // If no match is found, return null
        return null;
    }

    // Register method to add a new user to the database
    public Client register(String username, String password, String token, SocketChannel socket) {

        // Get the array of users from the database
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            String storedUsername = (String) user.get("username");

            // If the username already exists, return null
            if (storedUsername.equals(username)) {
                return null;
            }
        }

        // If the username is not taken, create a new JSONObject for the new user
        JSONObject newClient = new JSONObject();
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        newClient.put("username", username);
        newClient.put("password", passwordHash);
        newClient.put("token", token);
        newClient.put("rank", 0);

        // Add the new user to the array of users
        databaseArray.add(newClient);
        this.database.put("database", databaseArray);

        // Return a new Client object for the new user
        return new Client(username, passwordHash, token, 0L, socket);
    }

    // Reconnect method to restore a user's session based on their token
    public Client reconnect(String token, SocketChannel socket) {

        // Get the array of users from the database
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            String storedToken = (String) user.get("token");

            // Check if the stored token matches the provided token
            if (storedToken.equals(token)) {
                String username = (String) user.get("username");
                String password = (String) user.get("password");
                Long rank = ((Number) user.get("rank")).longValue();
                return new Client(username, password, token, rank, socket);
            }
        }
        // If no matching token is found, return null
        return null;
    }

    // Update the rank of a user in the database
    public void updateRank (Client client, int value) {
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            String username = (String) user.get("username");
            if (username.equals(client.getUsername())) {

                // If there is a match, update the user's rank
                Long rank = ((Number) user.get("rank")).longValue() + value;
                user.put("rank", rank);
                return;
            }
        }
    }

    //Invalidates the token for the given client
    public void invalidateToken(Client client) {
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            String username = (String) user.get("username");

            // If the username matches the username of the client whose token needs to be invalidated
            if (username.equals(client.getUsername())) {
                user.put("token", "");
                return;
            }
        }
    }

    public void resetTokens() {
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            user.put("token", "");
        }
    }

    public String[] getLeaderboard() {
        String[] leaderboard = new String[5];
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        List<JSONObject> userList = new ArrayList<>();
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            userList.add(user);
        }
        userList.sort((a, b) -> Long.compare(((Number) b.get("rank")).longValue(), ((Number) a.get("rank")).longValue()));
        for (int i = 0; i < 5 && i < userList.size(); i++) {
            JSONObject user = userList.get(i);
            String username = (String) user.get("username");
            long rank = ((Number) user.get("rank")).longValue();
            leaderboard[i] = username + " - " + rank;
        }
        return leaderboard;
    }
}