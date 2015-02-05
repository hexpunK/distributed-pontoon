package distributedpontoon.stubs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Jordan
 */
public class StubbedServer implements Runnable {
    
    private int port;
    private String hostName;
    private ServerSocket server;
    private Thread serverThread;

    public StubbedServer()
    {
        this.port = 50000;
        this.hostName = "UNKNOWN";
        this.server = null;
    }
    
    public StubbedServer(int port)
    {
        this.port = port;
        this.hostName = "UNKNOWN";
        this.server = null;
    }
    
    public void init()
    {
        try {
            server = new ServerSocket(port);
            hostName = server.getInetAddress().getHostName();
            
            serverThread = new Thread(this);
            serverThread.start();
            System.out.printf("Stubbed server now running on %s:%d...\n", hostName, port);
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    public void kill()
    {
        try {
            server.close();
            serverThread.join();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    @Override
    public void run() 
    {           
        System.out.println("Listening...");
        while (!server.isClosed())
        {
            try {
                Socket socket = server.accept();
                StubbedServerGame instance = new StubbedServerGame(socket);
                Thread t = new Thread(instance);
                t.start();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("StubbedServer - %s:%d", hostName, port);
    }
}
