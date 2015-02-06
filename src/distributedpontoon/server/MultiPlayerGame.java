package distributedpontoon.server;

import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author 6266215
 */
public class MultiPlayerGame extends IServerGame
{
    private final HashMap<Integer, Socket> sockets;
    private final HashMap<Integer, Hand> hands;
    private int playerCount;
    
    public MultiPlayerGame()
    {
        super();
        this.sockets = new HashMap<>();
        this.hands = new HashMap<>();
    }
    
    @Override
    public void registerPlayer(Socket socket)
    {        
        if (sockets.containsValue(socket)) return;
        
        int playerID = playerCount++;
        sockets.put(playerID, socket);
        hands.put(playerID, new Hand());
        
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            
            output.writeObject(MessageType.JOIN_ACKNOWLEDGE);
            output.writeInt(playerID);
            output.writeInt(gameID);
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException closeEx) {
                    System.err.println(closeEx.getMessage());
                }
            }
        }
    }
    
    @Override
    public void dealCard() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkHand(int playerID, Hand h) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void stop()
    {
        try {
            for (Socket socket : sockets.values())
                socket.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
