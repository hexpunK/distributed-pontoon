package distributedpontoon.directoryservice;

import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author 6266215
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
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                System.err.printf("ServerPoller failed to sleep:\n%s\n",
                        ex.getMessage());
            }
            System.out.println("Polling servers.");
            Set<Pair<String, Integer>> hosts = directory.getKnownHosts();
            for (final Pair host : hosts) {
                final String name = (String)host.getLeft();
                final int port = (int)host.getRight();
                Socket tmpSocket;
                try {
                    InetAddress address = InetAddress.getByName(name);
                    tmpSocket = new Socket(address, port);
                    ObjectOutputStream output = new ObjectOutputStream(tmpSocket.getOutputStream());
                    output.writeObject(MessageType.POLL_SERVER);
                    
                    ObjectInputStream input = new ObjectInputStream(tmpSocket.getInputStream());
                    output.flush();
                    boolean reply = input.readBoolean();
                } catch (UnknownHostException hostEx) {
                    directory.removeServer(name, port);
                    System.out.printf("Host %s does not seem to exist, "
                            + "removing.\n%s\n", name, hostEx.getMessage());
                } catch (IOException ioEx) {
                    directory.removeServer(name, port);
                    System.out.printf("Could not communicate with "
                            + "server %s, removing.\n%s\n", 
                            name, ioEx.getMessage());
                }
            }
        }
    }
    
}
