package distributedpontoon.directoryservice;

import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author 6266215
 * @version 1.0
 */
public class ServerPoller implements Runnable
{
    private boolean running;
    private final DirectoryService directory;
    
    public ServerPoller(DirectoryService directory)
    {
        this.directory = directory;
    }
    
    public void kill()
    {
        running = false;
    }
    
    @Override
    public void run()
    {
        running = true;
        while (running) {
            try {
                Thread.sleep(10000); // Poll every 10 seconds.
            } catch (InterruptedException ex) {
                System.err.printf("ServerPoller failed to sleep:\n%s\n",
                        ex.getMessage());
            }
            Set<Pair<String, Integer>> hosts = directory.getKnownHosts();
            ArrayList<Pair<String, Integer>> toRemove = new ArrayList<>();
            /* Check each server that we are already aware of. */
            for (final Pair host : hosts) {
                String name = (String)host.Left;
                int port = (int)host.Right;
                Socket tmpSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream input = null;
                try {
                    // Send a polling message to the running servers.
                    InetAddress address = InetAddress.getByName(name);
                    tmpSocket = new Socket(address, port);
                    out = new ObjectOutputStream(tmpSocket.getOutputStream());
                    out.writeObject(MessageType.POLL_SERVER);
                    
                    input = new ObjectInputStream(tmpSocket.getInputStream());
                    out.flush();
                    input.readBoolean();
                    // If any exceptions are thrown, remove the server.
                } catch (UnknownHostException hostEx) {
                    toRemove.add(host);
                    System.out.printf("Host %s does not seem to exist, "
                            + "removing.\n%s\n", name, hostEx.getMessage());
                } catch (IOException ioEx) {
                    toRemove.add(host);
                    System.out.printf("Could not communicate with "
                            + "server %s, removing.\n%s\n", 
                            name, ioEx.getMessage());
                } finally {
                    try {
                        if (input != null)
                            input.close();
                        if (out != null)
                            out.close();
                        if (tmpSocket != null)
                            tmpSocket.close();
                    } catch (IOException ex) {
                        System.err.printf("Could not close polling socket.\n%s",
                                ex.getMessage());
                    }
                }
            }
            /* Remove any servers that were lost whilst polling. */
            for (Pair host : toRemove) {
                directory.removeServer((String)host.Left, (int)host.Right);
            }
        }
    }
    
}
