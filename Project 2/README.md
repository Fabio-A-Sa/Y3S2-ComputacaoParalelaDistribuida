# Assignment 2
# Grade: 19.6/20

## Compilation

This project is available in Java SE version 17 or later.

The `libs/` folder should be added as a dependency directory.
In IntelliJ:<br>
File > Project Structure > Project Settings > assign2 > Modules > Dependencies > + > JAR and Directories > ~/path/to/libs/

## Run Server

```bash
$ java Server <PORT> <MODE> <DATABASE>
```

- PORT must be a valid port, ex: 8000
- MODE must be 0 or 1:
  - 0 - Simple mode;
  - 1 - Rank mode;
- DATABASE must be a JSON file in the `data/databases/` folder. If it does not exist it will be created by the server itself.

## Run Client Connection

```bash
$ java Connection <PORT> [HOST]
```

- PORT must be a valid port of an already running server, e.g. 8000
- HOST is an optional argument. By default, it is "localhost".

## Database

The database used by the server, represented in the Database class, manages the JSON format file given as an argument. The data has the following format:

```json
{"database": [
  {
    "password":"$2a$10$CwWvqxUPcw4Wy3kuIMxNau0h4wXfbBfqKY70dt293oubfVhJvKRO2",
    "rank":0,
    "username":"fabiosa",
    "token":"$2a$10$Fzx6iCSIomaUC\/7jRADNku67opT.ChmQdq9t\/8ZVigw0d.TkC.DXO"
  },
  { 
    "password":"$2a$10$5RlLQ7vX2wgBPHAUT\/8h6.vLTK6sKN6KVSPYb\/OlfY8jkelGGO9Vi",
    "rank":0,
    "username":"joaoaraujo",
    "token":"$2a$10$ej4oY5rwiuPAFvmAu9qire\/86ItbSSMeVnnJQnGijZrbfK7uk56OG"
  },
  {
    "password":"$2a$10$WI6c0MGeByOiJ5Jyv2ccKu9u0WvJtLxSXCPGbrvtT.0r8TR4TeVke",
    "rank":0,
    "username":"alexandrecorreia",
    "token":"$2a$10$Yiqe39oiAluUryZp\/PdzTOwwa2K7ILSmAl87lWVwNgu0mk5J3emeC"
  }
]}
```

The passwords are stored according to a BCrypt hash with *salt* for better security. The session tokens are generated with the hash of a string resulting from the concatenation of the username and an internal counter, which is incremented at each new global connection attempt on the server. This prevents having the same session tokens in several logins of the same user.<br>

The tokens are invalidated whenever:
- The client starts a new session. In this case they receive an updated token;
- The client finishes playing and does not want to play a new match;

## Authentication

After validating the connection to the server, four options appear on screen:
- 1 - Login
- 2 - Register
- 3 - Reconnect
- 4 - Quit

In the first two cases the user is asked to enter his access credentials (username and password).
In the third case the user will have to indicate the name of the file where he has his token, which is in the `data/tokens` folder. Usually the file is named `token-<USERNAME>.txt` and is sent after a successful login or registration.

All messages exchanged between the server and a client follow a fixed link protocol, this applies to the authentication process and also to the game itself:

| **Server Message**            | **Client Response** |
|-------------------------------|---------------------|
| FIN + Message                 | ACK                 |
| TKN + Message                 | Session Token Value |
| USR + Message                 | Username            |
| PSW + Message                 | Password            |
| OPT + Menu                    | Menu choice         |
| AUTH + tokenName + tokenValue | ACK                 |
| INFO + Message                | ACK                 |
| NACK + Message                | ACK                 |
| TURN + Message                | Any Input           |

The information being sent and received is validated according to this protocol. This task is performed by state machines on both sides of the established connection.

## Fault Tolerance

### 1 - DataBase

Server calls the `Database.backup()` method whenever there are changes at the client data layer (session token, socket, new clients). At that point the in-memory records are also saved in the original JSON file.

### 2 - Connections

In case the authentication process completes successfully, the server creates an object of the Client class that holds all the important connection data (username, password, tokenSession, rank, clientSocket). This object is placed in a waiting list with the other previously authenticated clients. 

While the client is waiting for a match in the game, the connection to the server can be lost. In that case he must use his session token, sent automatically at the time of the above operation, to be accepted again.
The evaluation function that managed this process has the following structure:
- if the client is new to the queue, adds it to the queue;
- if the client already exists in the queue (same username), then it stays in the same position and only the socket is updated. The old connection is closed;

```java
for (Client c : this.waiting_queue) {
    if (c.equals(client)) {

        // If the client is already in the queue, their socket is updated with the new one
        c.getSocket().close();
        c.setSocket(client.getSocket());
        System.out.println("Client " + client.getUsername() + " reconnected. Queue size: " + this.waiting_queue.size());
        return;
    }
}

// If the client is not already in the queue, add them to the end of the queue
this.waiting_queue.add(client);
System.out.println("Client " + client.getUsername() + " is now in waiting queue. Queue size: " + this.waiting_queue.size());
```

This way we can ensure that no player loses his place in the queue, this is very important in the Rank Mode.

#### 2.2 - Timeout

The server also has a timeout mechanism that closes the connection to the client if it does not respond to the server's requests within a certain time interval. This mechanism is implemented in the `Connection` class and is activated whenever the server sends a message to the client. If the client does not respond, the connection is closed server side.

The client also implements a timeout mechanism. If the client doesn't receive an answer from the server within a defined timeout, it closes the socket. This is done with the use of a `Selector` attached to the player's socket.

## Server Modes

### Simple

For this game mode, the system assigns the first group of ***n*** users that are in the waiting queue to the first game instance. This process continues for the following groups of ***n*** users, allocating them to the subsequent game instances, until the maximum game instances is reached.

### Rank

In this game mode, the system has to keep track of the rank of each user. 

When the server tries to assign a group of ***n*** players to a game, it assigns the players with the closest ranks to each other. 

If there is a big disparity of rank between users, the server will wait for further users to be added to the waiting queue, so that it can create a more balanced game. 

Although, after each try to create a game, the server will increase the threshold for rank disparities, so that users do not have to wait indeterminately for the next game.

