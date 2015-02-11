package distributedpontoon.directoryservice;

import distributedpontoon.server.Server;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author 6266215
 * @version 1.0
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
    private Set<Pair<String, Integer>> knownHosts;
    
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
    
    public void addServer(String hostName, int port)
    {
        knownHosts.add(new Pair<>(hostName, port));
    }
    
    public Set<Pair<String, Integer>> getKnownHosts() { return knownHosts; }
    
    public void removeServer(String hostName, int port)
    {
        Pair toRemove = null;
        for (Pair pair : knownHosts) {
            if (pair.Left.equals(hostName) && pair.Right.equals(port))
            {
                toRemove = pair;
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
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                
                MessageType request = (MessageType)input.readObject();
                switch (request) {
                    case QUERY_SERVERS:
                        System.out.println("Sending list of known hosts...");
                        output.writeObject(MessageType.QUERY_SERVERS);
                        output.writeObject(knownHosts);
                        output.flush();
                        break;
                    case REGISTER_SERVER:
                        System.out.println("Registering server...");
                        String remoteName = input.readUTF();
                        int remotePort = input.readInt();
                        addServer(remoteName, remotePort);
                        System.out.printf("Registerd server %s:%d\n", 
                                remoteName, remotePort);
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
        DirectoryService server;
        Integer port = null;
        /* If a port has been specified, attempt to start a server on it. */
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
}
