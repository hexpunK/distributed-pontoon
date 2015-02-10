package distributedpontoon.directoryservice;

import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

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
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.err.printf("ServerPoller failed to sleep:\n%s\n",
                        ex.getMessage());
            }
            System.out.println("Polling servers.");
            HashMap<String, Integer> hosts = directory.getKnownHosts();
            for (final String name : hosts.keySet()) {
                final int port = hosts.get(name);
                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Socket tmpSocket;
                        try {
                            InetAddress address = InetAddress.getByName(name);
                            tmpSocket = new Socket(address, port);
                            ObjectOutputStream output = new ObjectOutputStream(tmpSocket.getOutputStream());
                            ObjectInputStream input = new ObjectInputStream(tmpSocket.getInputStream());

                            output.writeObject(MessageType.POLL_SERVER);
                            output.flush();
                            MessageType reply = (MessageType)input.readObject();
                        } catch (UnknownHostException hostEx) {
                            directory.removeServer(name);
                            System.out.printf("Host %s does not seem to exist, "
                                    + "removing.\n%s\n", hostEx.getMessage());
                        } catch (IOException ioEx) {
                            directory.removeServer(name);
                            System.out.printf("Could not communicate with "
                                    + "server %s, removing.\n%s\n", 
                                    ioEx.getMessage());
                        } catch (ClassNotFoundException cnfEx) {
                            System.err.println(cnfEx.getMessage());
                        }
                    }
                });
                t.start();
            }
        }
    }
    
}
