package distributedpontoon.server;

import distributedpontoon.directoryservice.DirectoryService;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import distributedpontoon.shared.PontoonLogger;
import distributedpontoon.shared.Triple;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A TCP server that listens for connections on a specified port. When a client 
 * connects a new {@link IServerGame} instance will be launched for that client 
 * to play a game against.
 * 
 * @author 6266215
 * @version 1.4
 * @since 2015-02-21
 */
public class Server implements Runnable
{
    /** 
     * A singleton instance of the server to prevent multiple copies running 
     * per process.
     */
    private static Server INSTANCE;
    /** The TCP port to listen for connections on. */
    private final int port;
    /** The name/IP address of the host running this server. */
    private String hostName;
    /** TCP Socket to listen for connections. */
    private ServerSocket server;
    /** A thread to allow this {@link Server} to run in the background. */
    private Thread serverThread;
    /** The host name of the directory server. */
    private String dirServer;
    /** The port of the directory server. */
    private int dirPort;
    /** A mapping of {@link IServerGame} instances to their executing thread. */
    private final ConcurrentHashMap<IServerGame, Thread> games;
    /** The credits the {@link IServerGame}s can use for paying out. */
    private int bank;
    
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /**
     * Creates a new instance of {@link Server} listening on port 50,000.
     * 
     * @since 1.0
     */
    private Server()
    {
        this.port = 55551;
        this.hostName = "UNKNOWN";
        this.server = null;
        this.serverThread = null;
        this.dirServer = "localhost";
        this.dirPort = 55552;
        this.games = new ConcurrentHashMap<>();
        this.bank = 50000;
    }
    
    /**
     * Creates a new instance of {@link Server} that listens on the specified 
     * port. If the port is negative 
     * 
     * @param port The port to listen on as an int.
     * @throws IllegalArgumentException Thrown if the specified port is invalid 
     * for TCP sockets.
     * @since 1.0
     */
    private Server(int port) throws IllegalArgumentException
    {
        if (port < 0 || port > 65536) {
            throw new IllegalArgumentException(
                    "Port number must be between 0 and 65536"
            );
        }
        
        this.port = port;
        this.hostName = "UNKNOWN";
        this.server = null;
        this.serverThread = null;
        this.dirServer = "localhost";
        this.dirPort = 55552;
        this.games = new ConcurrentHashMap<>();
        this.bank = 50000;
    }
    
    /**
     * Creates a Singleton instance of the {@link Server} class for this 
     * process. The returned {@link Server} object is the default construction, 
     * and will listen on port 50,000.
     * 
     * @return A new instance of {@link Server} if none exists, or the currently
     *  running instance of one has been created before.
     * @since 1.0
     */
    public synchronized static Server getInstance()
    {
        if (Server.INSTANCE == null) {
            Server.INSTANCE = new Server();
        }        
        return Server.INSTANCE;
    }
    
    /**
     * Creates a Singleton instance of the {@link Server} class for this 
     * process. The returned {@link Server} object will listen on the specified 
     * port.
     * 
     * @param port The port to listen on as an int.
     * @return A new instance of {@link Server} if none exists, or the currently
     *  running instance of one has been created before.
     * @throws IllegalArgumentException Thrown if the specified port is invalid 
     * for TCP sockets.
     * @since 1.0
     */
    public synchronized static Server getInstance(int port) 
            throws IllegalArgumentException
    {
        if (Server.INSTANCE == null) {
            Server.INSTANCE = new Server(port);
        }        
        return Server.INSTANCE;
    }
    
    /**
     * Sets the details of the {@link DirectoryService} to connect to. The 
     * host name and port should both be specified when calling this to ensure 
     * that they are both correct.
     * 
     * @param dirName The host name/ IP address of the {@link DirectoryService} 
     * as a String.
     * @param dirPortNum The port number the {@link DirectoryService} listens on
     *  as an int.
     * @since 1.1
     */
    public void setDirectoryServer(String dirName, int dirPortNum)
    {
        this.dirServer = dirName;
        this.dirPort = dirPortNum;
    }
    
    /**
     * Starts running this {@link Server} instance. Begins the server thread to 
     * allow it to listen for connections in the background.
     * 
     * @since 1.0
     */
    public void init()
    {
        try {
            PontoonLogger.setup("server");
        } catch (IOException ex) {
            serverError("Error setting up logging. Reason%n%s", 
                    ex.getMessage());
        }
        serverMessage("Starting server...");
        try {
            server = new ServerSocket(port);
            hostName = InetAddress.getLocalHost().getHostName();
            
            serverThread = new Thread(this);
            serverThread.start();
            serverMessage("Server started.");
        } catch (IOException ioEx) {
            serverError("Error intiialising server. Reason:%n\t%s", 
                    ioEx.getMessage());
        }
    }
    
    /**
     * Stops this {@link Server} instance. Tells all the running games to end 
     * before closing the connection.
     * 
     * @since 1.0
     */
    public void kill()
    {
        serverMessage("Shutting down server...");
        
        Set<IServerGame> gameSet = games.keySet();
        for (IServerGame game : gameSet) {
            game.stop();
            Thread t = games.get(game);
            if (t != null) {
                try {
                    t.join();
                } catch (InterruptedException intEx) {
                    serverError("Game interrupted unexpectedly.");
                }
            }
        }
        
        try {
            server.close();
            serverThread.join();
            serverMessage("Server shut down.");
        } catch (IOException ioEx) {
            serverMessage("Failed to close the server socket.");
        } catch (InterruptedException intEx) {
            serverMessage("Server thread was interrupted incorrectly.");
        }
        
        try {
            PontoonLogger.close();
        } catch (IOException ex) {
            serverError("Failed to close logger. Reason:%n%s", ex.getMessage());
        }
    }
    
    /**
     * Prints information messages to the current {@link Logger} output.
     * 
     * @param level The logging {@link Level} to use.
     * @param msg The message to print as a String. Accepts formatting 
     * parameters similarly to {@link String#format(java.lang.String, 
     * java.lang.Object...)}.
     * @param args Any number of objects to print in the resulting message. For 
     * objects to print useful data it may require overriding the {@link 
     * Object#toString()} method.
     * @since 1.4
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    private synchronized void serverMessage(Level level, String msg, 
            Object...args)
    {
        msg = String.format(msg, args);
        logger.log(Level.INFO, "{0}", new Object[] {msg});
    }
    
    /**
     * Prints information messages to the current {@link Logger} output. Logs to
     *  the {@link Level#INFO} logger level.
     * 
     * @param msg The message to print as a String. Accepts formatting 
     * parameters similarly to {@link String#format(java.lang.String, 
     * java.lang.Object...)}.
     * @param args Any number of objects to print in the resulting message. For 
     * objects to print useful data it may require overriding the {@link 
     * Object#toString()} method.
     * @since 1.0
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    private synchronized void serverMessage(String msg, Object...args)
    {
        serverMessage(Level.INFO, msg, args);
    }
    
    /**
     * Prints information messages to the current {@link Logger} output.
     * 
     * @param msg The message to print as a String. Accepts formatting 
     * parameters similarly to {@link String#format(java.lang.String, 
     * java.lang.Object...)}.
     * @param args Any number of objects to print in the resulting message. For 
     * objects to print useful data it may require overriding the {@link 
     * Object#toString()} method.
     * @since 1.0
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    private synchronized void serverError(String msg, Object...args)
    {
        msg = String.format(msg, args);
        logger.log(Level.WARNING, "{0}", new Object[] {msg});
    }
    
    /**
     * Attempt to register this {@link Server} on the specified {@link 
     * DirectoryService}. If this fails the server will continue to run, though 
     * accessing it will require knowing the details of the server.
     * 
     * @since 1.1
     */
    private void registerServer()
    {
        Socket directorySocket;
        try {
            InetAddress address = InetAddress.getByName(dirServer);
            directorySocket = new Socket(address, dirPort);
            ObjectOutputStream output = 
                    new ObjectOutputStream(directorySocket.getOutputStream());
            
            serverMessage("Registering with directory server...");
            output.writeObject(MessageType.REGISTER_SERVER);
            output.writeUTF(hostName);
            output.writeInt(port);
            output.flush();
        } catch (UnknownHostException hostEx) {
            serverError("Directory server host '%s' may not exist.", 
                    dirServer);
        } catch (IOException ioEx) {
            serverError("Could not register with directory server.");
        }
    }
    
    /**
     * Attempt to register a {@link MultiPlayerGame} on the specified {@link 
     * DirectoryService}. If this isn't possible, the game will be inaccessible 
     * to other players.
     * 
     * @param id The game ID for the {@link MultiPlayerGame} being registered.
     * @since 1.1
     */
    public void registerGame(int id)
    {
        Socket directorySocket;
        try {
            InetAddress address = InetAddress.getByName(dirServer);
            directorySocket = new Socket(address, dirPort);
            ObjectOutputStream output = 
                    new ObjectOutputStream(directorySocket.getOutputStream());
            
            serverMessage("Registering game %d with directory server...", id);
            output.writeObject(MessageType.REGISTER_GAME);
            output.writeUTF(hostName);
            output.writeInt(port);
            output.writeInt(id);
            output.flush();
        } catch (UnknownHostException hostEx) {
            serverError("Directory server host '%s' may not exist.", 
                    dirServer);
        } catch (IOException ioEx) {
            serverError("Could not register game with directory server.");
        }
    }
    
    /**
     * Removes a game from the {@link DirectoryService} this {@link Server} is 
     * connected to.
     * 
     * @param id The game ID of the {@link IServerGame} to remove.
     * @since 1.2
     */
    public void unregisterGame(int id)
    {
        Socket directorySocket;
        
        try {
            InetAddress address = InetAddress.getByName(dirServer);
            directorySocket = new Socket(address, dirPort);
            ObjectOutputStream output = 
                    new ObjectOutputStream(directorySocket.getOutputStream());
            
            serverMessage("Unregistering game %d with directory server...", id);
            output.writeObject(MessageType.UNREGISTER_GAME);
            output.writeUTF(hostName);
            output.writeInt(port);
            output.writeInt(id);
            output.flush();
        } catch (UnknownHostException hostEx) {
            serverError("Directory server host '%s' may not exist.", 
                    dirServer);
        } catch (IOException ioEx) {
            serverError("Could not unregister game with directory server.");
        } finally {
            removeGame(id);
        }
    }
    
    /**
     * Removes a specified {@link IServerGame} and its assigned {@link Thread} 
     * from the stored mapping in this {@link Server}.
     * 
     * @param id The unique ID of the {@link IServerGame} to remove as an int.
     * @since 1.3
     */
    public void removeGame(int id)
    {
        IServerGame game = null;
        for (IServerGame g : games.keySet())
            if (g.getGameID() == id) {
                game = g;
                break;
            }
        /* Attempt to shut the game down safely. */
        if (game != null) {
            Thread t = games.get(game);
            game.stop();
            try {
                t.join(2000);
            } catch (InterruptedException ex) {
                serverError("Could not safely join thread for game %d", id);
            }
            games.remove(game);
            serverMessage("Removed game %d.", id);
        }
    }
    
    /**
     * Gets the free credits this {@link Server} has in its bank.
     * 
     * @return Returns the amount of credits available as an int.
     * @since 1.4
     */
    public synchronized int getBank() { return this.bank; }
    
    /**
     * Adjusts the amount of credits this {@link Server} has stored in its bank.
     *  To remove credits, simply provide a negative value. Synchronises the 
     * changes in this servers bank with the other servers.
     * 
     * @param delta The number of credits to adjust the bank balance by as an 
     * int.
     * @return Returns true if this {@link Server} has more money in the bank.
     * @since 1.5
     */
    public synchronized boolean adjustBank(int delta)
    {   
        return adjustBank(delta, true);
    }
    
    /**
     * Adjusts the amount of credits this {@link Server} has stored in its bank.
     *  To remove credits, simply provide a negative value.
     * 
     * @param delta The number of credits to adjust the bank balance by as an 
     * int.
     * @param sync Set to true to propagate this change to other {@link Server} 
     * instances the {@link DirectoryService} is aware of.
     * @return Returns true if this {@link Server} has more money in the bank.
     * @since 1.4
     */
    public synchronized boolean adjustBank(int delta, boolean sync)
    {   
        // Synchronise this change to other servers if needed.
        if (sync)
            synchroniseBank(delta);
        
        bank += delta;        
        if (bank <= 0) {
            serverMessage("The bank has run out of credits!");
            return false;
        }
        return true;
    }
    
    /**
     * Attempts to synchronise the changes in the bank balance between all the 
     * currently known {@link Server}s, this is only called when a game attempts
     *  to run {@link Server#adjustBank(int, boolean)}.
     * 
     * @param delta The number of credits to add or remove on the remote server.
     * @since 1.5
     */
    public synchronized void synchroniseBank(int delta)
    {
        InetAddress address;
        Socket socket = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        Set<Triple<String, Integer, Integer>> servers;
        Set<Pair<String, Integer>> updated = new HashSet<>();
        
        try {
            address = InetAddress.getByName(dirServer);
            socket = new Socket(address, dirPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            
            output.writeObject(MessageType.QUERY_SERVERS);
            output.flush();
            
            if ((MessageType)input.readObject() != MessageType.QUERY_SERVERS)
                return;
            servers = (Set)input.readObject();
            for (Triple<String, Integer, Integer> svr : servers) {
                if (svr.One.equals(this.hostName) && svr.Two == this.port)
                    continue; // Avoid sending this to yourself.
                Pair<String, Integer> svrInfo = new Pair<>(svr.One, svr.Two);
                if (!updated.contains(svrInfo)) {
                    updated.add(svrInfo); // Keep track of all unique servers.
                    address = InetAddress.getByName(svrInfo.Left);
                    socket = new Socket(address, svrInfo.Right);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    // Tell the server how much to adjust its bank by.
                    output.writeObject(MessageType.UPDATE_BANK);
                    output.writeInt(delta);
                    output.flush();
                }
            }
        } catch (UnknownHostException hostEx) {
            serverError("Directory server host '%s' may not exist.", 
                    dirServer);
        } catch (IOException | ClassNotFoundException ioEx) {
            serverError("Could not update other server banks. Reason:%n%s", 
                    ioEx.getMessage());
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
                if (socket != null)
                    socket.close();
            } catch (IOException closeEx) {
                serverError("Failed to close socket when syncing banks.");
            }
        }
    }
    
    /**
     * Listens for connections in the background and launches new games when 
     * a connection is attempted.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        registerServer();
        serverMessage("Server listening (%s:%d).", hostName, 
                server.getLocalPort());
        while (!server.isClosed())
        {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException ioEx) {
                serverError("Server could not connect to client. Reason:%n\t%s",
                        ioEx.getMessage());
                continue;
            }
            
            try {
                ObjectInputStream input = 
                        new ObjectInputStream(socket.getInputStream());
                MessageType query = (MessageType)input.readObject();
                IServerGame game = null;
                Thread t;
                switch (query) {
                    case POLL_SERVER:
                        // Respond to polling from DirectoryServices.
                        ObjectOutputStream reply = 
                            new ObjectOutputStream(socket.getOutputStream());
                        reply.writeBoolean(true);
                        reply.flush();
                        break;
                    case UPDATE_BANK:
                        int delta = input.readInt();
                        serverMessage("Remote bank change of %d", delta);
                        adjustBank(delta, false);
                        break;
                    case CLIENT_JOIN_SP:
                        // Set up single-player games.
                        serverMessage("Client %s connecting...", 
                                socket.getInetAddress().getHostName());
                        game = new SinglePlayerGame();
                        serverMessage("Client %s registered to game %d.", 
                                socket.getInetAddress().getHostName(), 
                                game.getGameID());
                        game.registerPlayer(socket);

                        t = new Thread(game);
                        t.start();
                        games.put(game, t);
                        break;
                    case CLIENT_JOIN_MP:
                        // Set up multi-player games.
                        serverMessage("Client %s connecting...", 
                                socket.getInetAddress().getHostName());
                        int gameID = input.readInt();
                        if (gameID <= 0) {
                            // No valid game ID? Make a new one!
                            serverMessage("Starting new MP game...");
                            game = new MultiPlayerGame();
                            t = new Thread(game);
                            t.start();
                            games.put(game, t);
                        } else {
                            // Otherwise try and find a valid game.
                            boolean gFound = false;
                            for (IServerGame g : games.keySet()) {
                                if (g.getGameID() == gameID) {
                                    serverMessage("Joining MP game %d...", 
                                            gameID);
                                    game = g;
                                    gFound = true;
                                    break;
                                }
                            }
                            if (!gFound) {
                                game = null;
                                serverError("No MP game with the ID %d "
                                        + "found.", gameID);
                            }
                        }
                        if (game != null) {
                            serverMessage("Client %s registered to game %d.", 
                                socket.getInetAddress().getHostName(), 
                                game.getGameID());
                            game.registerPlayer(socket);
                        }
                        break;
                    default:
                        serverError("Unknown message %s received.", query);
                }
            } catch (IOException ioEx) {
                serverError("Communication error: %s", ioEx.getMessage());
            } catch (ClassNotFoundException cnfEx) {
                serverError("Unknown object type recieved.%n%s",
                        cnfEx.getMessage());
            }
        }
    }
    
    /**
     * Creates a new instance of a {@link Server}.
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {        
        Integer port = null;
        String dirServerName = "localhost";
        int dirServerPort = 55552;
        /* handle the command line parameters if any were passed. */
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-p":
                case "--port":
                    // Allow the port to be specified.
                    try {
                        port = Integer.parseInt(args[i+1]);
                        i++;
                    } catch (NumberFormatException nEx) {
                        System.err.println("Port value must be a number.");
                    }
                    break;
                case "--dir-server":
                    String[] parts = args[++i].split(":");
                    if (parts.length == 1) {
                        dirServerName = parts[0];
                    } else {
                        try {
                            dirServerName = parts[0];
                            dirServerPort = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException nfEx) {
                            System.err.println("Port value must be a number.");
                        }
                    }
                    break;
                case "--no-file":
                    System.out.println("Disabling logging to file...");
                    PontoonLogger.fileLog = false;
                    break;
                case "-v":
                case "--verbose":
                    System.out.println("Enabling verbose output...");
                    PontoonLogger.verbose = true;
                    break;
                case "-h":
                case "--help":
                    // Show a help message.
                    System.out.println(helpMessage());
                    return;
                default:
                    System.err.printf("Unknown argument '%s'%n", args[i]);
            }
        }
        
        Server server;
        /* If a port has been specified, attempt to start a server on it. */
        if (port != null) {
            try {
                server = Server.getInstance(port);
            } catch (IllegalArgumentException argEx) {
                System.err.println(argEx.getMessage());
                return;
            }
        } else {
            server = Server.getInstance();
        }
        
        server.setDirectoryServer(dirServerName, dirServerPort);
        server.init(); // Start running the server.
        
        Scanner input = new Scanner(System.in);
        String line;
        boolean running = true;
        /* Check for commands provided through the CLI. */
        while (running) {
            line = input.nextLine().trim();
            switch (line) {
                case "bal":
                    System.out.printf("Current Bank: %d%n", server.getBank());
                    break;
                case "q":
                case "quit":
                    running = false;
                    break;
                default:
                    System.out.printf("Unknown command '%s'.%n", line);
            }
        }
        server.kill();
    }
    
    /**
     * Creates a help message for the {@link Server} command line arguments.
     * 
     * @return A String containing the help message.
     * @since 1.0
     */
    private static String helpMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Distributed Pontoon Server Help:\n");
        sb.append("\tCommand [options] (Short) - Action\n");
        sb.append("\t--port [port] (-p) - Specifies the port to listen on.\n");
        sb.append("\t--dir-server [hostname:port] - Sets the directory server "
                + "to connect to. If no port is specific, port 55552 is used.");
        sb.append("\n\t--no-file - Prevents logging to a file.\n");
        sb.append("\t--verbose (-v) - Prints extra detail to the console.");
        sb.append("\t--help (-h) - Displays this help message.\n");
        
        return sb.toString();
    }
}
