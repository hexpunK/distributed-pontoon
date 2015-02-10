package distributedpontoon.server;

import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/**
 * A TCP server that listens for connections on a specified port. When a client 
 * connects a new {@link IServerGame} instance will be launched for that client 
 * to play a game against.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
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
    /** A mapping of {@link IServerGame} instances to their executing thread. */
    private final HashMap<IServerGame, Thread> games;
    
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
        this.games = new HashMap<>();
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
        this.games = new HashMap<>();
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
     * Starts running this {@link Server} instance. Begins the server thread to 
     * allow it to listen for connections in the background.
     * 
     * @since 1.0
     */
    public void init()
    {
        serverMessage("Starting server...");
        try {
            server = new ServerSocket(port);
            hostName = InetAddress.getLocalHost().getHostName();
            
            serverThread = new Thread(this);
            serverThread.start();
            serverMessage("Server started.");
        } catch (IOException ioEx) {
            serverError("Error intiialising server. Reason:\n\t%s", 
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
            try {
                t.join();
            } catch (InterruptedException intEx) {
                serverError("Game interrupted unexpectedly.");
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
    }
    
    /**
     * Prints information messages to the current {@link System#out} output 
     * stream.
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
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String formattedDate = fmt.format(cal.getTime());
        msg = String.format(msg, args);
        System.out.printf("SERVER (%s): %s\n", formattedDate, msg);
    }
    
    /**
     * Prints information messages to the current {@link System#err} output 
     * stream.
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
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String formattedDate = fmt.format(cal.getTime());
        msg = String.format(msg, args);
        System.err.printf("SERVER (%s): %s\n", formattedDate, msg);
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
        serverMessage("Registering with directory server...");
        String serverName = "CMPLAB2-04";
        int directoryPort = 55552;
        Socket directorySocket;
        try {
            InetAddress address = InetAddress.getByName(serverName);
            directorySocket = new Socket(address, directoryPort);
            ObjectOutputStream output = new ObjectOutputStream(directorySocket.getOutputStream());
            
            output.writeObject(MessageType.REGISTER_SERVER);
            output.writeUTF(hostName);
            output.writeInt(port);
            output.flush();
        } catch (UnknownHostException hostEx) {
            System.err.println(hostEx.getMessage());
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
        serverMessage("Server listening (%s:%d).", hostName, 
                server.getLocalPort());
        while (!server.isClosed())
        {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException ioEx) {
                serverError("Server could not connect to client. Reason:\n\t%s",
                        ioEx.getMessage());
                continue;
            }
            serverMessage("Client %s connecting...", 
                    socket.getInetAddress().getHostName());
            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                MessageType query = (MessageType)input.readObject();
                switch (query) {
                    case POLL_SERVER:
                        ObjectOutputStream reply = new ObjectOutputStream(socket.getOutputStream());
                        reply.writeObject(MessageType.POLL_SERVER);
                        reply.flush();
                        break;
                    default:
                        IServerGame game = new SinglePlayerGame();
                        game.registerPlayer(socket);

                        Thread t = new Thread(game);
                        t.start();
                        games.put(game, t);
                }
            } catch (IOException ioEx) {
                System.err.printf("Error: %s\n", ioEx.getMessage());
            } catch (ClassNotFoundException cnfEx) {
                System.err.printf("Unknown object type recieved.\n%s\n",
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
                case "-h":
                case "--help":
                    // Show a help message.
                    System.out.println(helpMessage());
                    break;
                default:
                    System.err.printf("Unknown argument '%s'\n", args[i]);
            }
        }
        
        Server server = null;
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
        
        server.init(); // Start running the server.
        
        Scanner input = new Scanner(System.in);
        String line;
        boolean running = true;
        /* Check for commands provided through the CLI. */
        while (running) {
            line = input.nextLine().trim();
            switch (line) {
                case "q":
                case "quit":
                    running = false;
                    break;
                default:
                    System.out.printf("Unknown command '%s'.\n", line);
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
        sb.append("\t--help (-h) - Displays this help message.\n");
        
        return sb.toString();
    }
}
