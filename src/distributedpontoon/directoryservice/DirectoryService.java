package distributedpontoon.directoryservice;

import distributedpontoon.client.IPlayer;
import distributedpontoon.server.Server;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import distributedpontoon.shared.Triple;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Launches and manages a new {@link DirectoryService} to be used by 
 * {@link Server}s and {@link IPlayer}s when looking for games of Pontoon to 
 * play.
 * 
 * @author 6266215
 * @version 1.1
 */
public class DirectoryService implements Runnable
{
    /** 
     * A singleton instance of the server to prevent multiple copies running 
     * per process.
     */
    private static DirectoryService INSTANCE;
    /** Monitors the known servers and removes unresponsive ones. */
    private ServerPoller monitor;
    /** The TCP port to listen for connections on. */
    private final int port;
    /** The name/IP address of the host running this server. */
    private String hostName;
    /** TCP Socket to listen for connections. */
    private ServerSocket server;
    /** A thread to run {@link DirectoryService} in the background. */
    private Thread serverThread;
    /** A mapping of known host names to their ports. */
    private Set<Triple<String, Integer, Integer>> knownHosts;
    
    private DirectoryService()
    {
        this.port = 55552;
        this.hostName = "UNKNOWN";
        this.server = null;
        this.serverThread = null;
        this.knownHosts = new HashSet<>();
    }
    
    private DirectoryService(int port) throws IllegalArgumentException
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
        this.knownHosts = new HashSet<>();
    }
    
    public synchronized static DirectoryService getInstance()
    {
        if (DirectoryService.INSTANCE == null) {
            DirectoryService.INSTANCE = new DirectoryService();
        }
        return DirectoryService.INSTANCE;
    }
    
    public synchronized static DirectoryService getInstance(int port)
    {
        if (DirectoryService.INSTANCE == null) {
            DirectoryService.INSTANCE = new DirectoryService(port);
        }
        return DirectoryService.INSTANCE;
    }
    
    /**
     * Starts running this {@link Server} instance. Begins the server thread to 
     * allow it to listen for connections in the background.
     * 
     * @since 1.0
     */
    public void init()
    {
        System.out.println("Starting directory server...");
        try {
            server = new ServerSocket(port);
            hostName = InetAddress.getLocalHost().getHostName();
            
            serverThread = new Thread(this);
            serverThread.start();
            System.out.println("Server started.");
            monitor = new ServerPoller(this);
            Thread monitorThread = new Thread(monitor);
            monitorThread.start();
            System.out.println("Server monitor started.");
        } catch (IOException ioEx) {
            System.out.printf("Error intiialising server. Reason:\n\t%s\n", 
                    ioEx.getMessage());
        }
    }
    
    public void addServer(String hostName, int port, int gameID)
    {
        knownHosts.add(new Triple<>(hostName, port, gameID));
    }
    
    public Set<Triple<String, Integer, Integer>> getKnownHosts() 
    { 
        return knownHosts; 
    }
    
    public void removeServer(String hostName, int port)
    {
        Triple toRemove = null;
        for (Triple host : knownHosts) {
            if (host.One.equals(hostName) && host.Two.equals(port))
            {
                toRemove = host;
                break;
            }
        }
        if (toRemove != null) knownHosts.remove(toRemove);
    }
    
    /**
     * Stops this {@link Server} instance. Tells all the running games to end 
     * before closing the connection.
     * 
     * @since 1.0
     */
    public void kill()
    {
        System.out.println("Shutting down server...");
        
        try {
            monitor.kill();
            server.close();
            serverThread.join();
            System.out.println("Server shut down.");
        } catch (IOException ioEx) {
            System.out.println("Failed to close the server socket.");
        } catch (InterruptedException intEx) {
            System.out.println("Server thread was interrupted incorrectly.");
        }
    }

    @Override
    public void run() 
    {
        System.out.printf("Server listening (%s:%d).\n", hostName, 
                server.getLocalPort());
        while (!server.isClosed())
        {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException ioEx) {
                System.out.printf(
                        "Server could not connect to client. Reason:\n\t%s\n",
                        ioEx.getMessage());
                continue;
            }
            System.out.printf("Client %s connecting...\n", 
                    socket.getInetAddress().getHostName());
            try {
                ObjectOutputStream output = 
                        new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = 
                        new ObjectInputStream(socket.getInputStream());
                
                MessageType request = (MessageType)input.readObject();
                String remoteName;
                int remotePort;
                switch (request) {
                    case QUERY_SERVERS:
                        System.out.println("Sending list of known hosts...");
                        output.writeObject(MessageType.QUERY_SERVERS);
                        output.writeObject(knownHosts);
                        output.flush();
                        break;
                    case REGISTER_SERVER:
                        System.out.println("Registering server...");
                        remoteName = input.readUTF();
                        remotePort = input.readInt();
                        addServer(remoteName, remotePort, -1);
                        addServer(remoteName, remotePort, 0);
                        System.out.printf("Registered server %s:%d\n", 
                                remoteName, remotePort);
                        break;
                    case REGISTER_GAME:
                        System.out.println("Registering game...");
                        remoteName = input.readUTF();
                        remotePort = input.readInt();
                        int gameID = input.readInt();
                        addServer(remoteName, remotePort, gameID);
                        System.out.printf("Registered game %s:%d - %d\n", 
                                remoteName, remotePort, gameID);
                        break;
                    default:
                        System.err.printf("Directory server does not support "
                                + "message %s!\n", request);
                }
            } catch (IOException ioEx) {
                System.err.printf("Error: %s\n", ioEx.getMessage());
            } catch (ClassNotFoundException cnfEx) {
                System.err.printf("Unknown object type recieved.\n%s\n",
                        cnfEx.getMessage());
            }
        }
    }
    
    public static void main(String[] args)
    {
        Integer port = null;
        DirectoryService server;
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
        
        if (port != null) {
            try {
                server = DirectoryService.getInstance(port);
            } catch (IllegalArgumentException argEx) {
                System.err.println(argEx.getMessage());
                return;
            }
        } else {
            server = DirectoryService.getInstance();
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
     * Creates a help message for the {@link DirectoryService} command line 
     * arguments.
     * 
     * @return A String containing the help message.
     * @since 1.1
     */
    private static String helpMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Pontoon Directory Server Help:\n");
        sb.append("\tCommand [options] (Short) - Action\n");
        sb.append("\t--port [port] (-p) - Specifies the port to listen on.\n");
        sb.append("\t--help (-h) - Displays this help message.\n");
        
        return sb.toString();
    }
}
