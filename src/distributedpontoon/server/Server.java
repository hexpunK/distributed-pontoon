package distributedpontoon.server;

import distributedpontoon.shared.IServerGame;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author 6266215
 */
public class Server implements Runnable
{
    private final int port;
    private String hostName;
    private ServerSocket server;
    private Thread serverThread;
    private final HashMap<IServerGame, Thread> games;
    
    public Server()
    {
        this.port = 50000;
        this.hostName = "UNKNOWN";
        this.server = null;
        this.serverThread = null;
        this.games = new HashMap<>();
    }
    
    public Server(int port)
    {
        this.port = port;
        this.hostName = "UNKNOWN";
        this.server = null;
        this.serverThread = null;
        this.games = new HashMap<>();
    }
    
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
    
    private void serverMessage(String msg, Object...args)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String formattedDate = fmt.format(cal.getTime());
        msg = String.format(msg, args);
        System.out.printf("SERVER (%s): %s\n", formattedDate, msg);
    }
    
    private void serverError(String msg, Object...args)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String formattedDate = fmt.format(cal.getTime());
        msg = String.format(msg, args);
        System.err.printf("SERVER (%s): %s\n", formattedDate, msg);
    }
    
    @Override
    public void run()
    {
        serverMessage("Server listening (%s:%d).", 
                hostName, server.getLocalPort());
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
            IServerGame game = new SinglePlayerGame();
            game.registerPlayer(socket);
            
            Thread t = new Thread(game);
            t.start();
            games.put(game, t);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {        
        Server server = new Server();
        server.init();
        
        Scanner input = new Scanner(System.in);
        String line;
        
        while ((line = input.nextLine()) != null) {
            line = line.trim();
            switch (line) {
                case "q":
                case "quit":
                    server.kill();
                    System.exit(0);
                    break;
                default:
                    System.out.printf("Unknown command '%s'.\n", line);
            }
        }
    }
    
}
