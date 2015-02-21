package distributedpontoon.directoryservice;

import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Triple;
import distributedpontoon.server.Server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Runs in the background of a {@link DirectoryService} process and periodically
 *  polls all the known {@link Server}s to see if they are still responding.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-12
 */
public class ServerPoller implements Runnable
{
    /** The amount of time in milliseconds to wait before polling again. */
    public static final long POLL_DELAY = 10000;
    /** Keeps this {@link ServerPoller} running in the background. */
    private boolean running;
    /** The {@link DirectoryService} to get known servers from. */
    private final DirectoryService directory;
    
    /**
     * Creates a new {@link ServerPoller} that uses the specified {@link 
     * DirectoryService} as its hosts list.
     * 
     * @param directory The {@link DirectoryService} to use.
     * @since 1.0
     */
    public ServerPoller(DirectoryService directory)
    {
        this.directory = directory;
    }
    
    /**
     * Stops this {@link ServerPoller} instance from running.
     * 
     * @since 1.0
     */
    public void kill()
    {
        running = false;
    }
    
    /**
     * Periodically sends out {@link MessageType#POLL_SERVER} messages to known 
     * {@link Server}s.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        running = true;
        while (running) {
            try {
                Thread.sleep(POLL_DELAY);
            } catch (InterruptedException ex) {
                System.err.printf("ServerPoller failed to sleep:%n%s%n",
                        ex.getMessage());
            }
            Set<Triple<String, Integer, Integer>> hosts = 
                    directory.getKnownHosts();
            ArrayList<Triple> toRemove = new ArrayList<>();
            /* Check each server that we are already aware of. */
            for (final Triple host : hosts) {
                String name = (String)host.One;
                int port = (int)host.Two;
                Socket tmpSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream input = null;
                try {
                    // Send a polling message to the running servers.
                    InetAddress address = InetAddress.getByName(name);
                    tmpSocket = new Socket(address, port);
                    out = new ObjectOutputStream(tmpSocket.getOutputStream());
                    out.writeObject(MessageType.POLL_SERVER);
                    out.flush();
                    // Read the response from the polled server.
                    input = new ObjectInputStream(tmpSocket.getInputStream());
                    input.readBoolean();
                    // If any exceptions are thrown, remove the server.
                } catch (UnknownHostException hostEx) {
                    toRemove.add(host);
                    System.out.printf("Host %s does not seem to exist, "
                            + "removing.%n%s%n", name, hostEx.getMessage());
                } catch (IOException ioEx) {
                    toRemove.add(host);
                    System.out.printf("Could not communicate with server %s, "
                            + "removing.%n%s%n", 
                            name, ioEx.getMessage());
                } finally {
                    // Safely close the polling connections.
                    try {
                        if (input != null)
                            input.close();
                        if (out != null)
                            out.close();
                        if (tmpSocket != null)
                            tmpSocket.close();
                    } catch (IOException ex) {
                        System.err.printf("Could not close polling socket.%n%s",
                                ex.getMessage());
                    }
                }
            }
            /* Remove any servers that were lost whilst polling. */
            for (Triple host : toRemove) {
                directory.removeServer((String)host.One, (int)host.Two, -1);
            }
        }
    }
    
}
