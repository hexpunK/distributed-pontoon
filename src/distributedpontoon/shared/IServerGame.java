package distributedpontoon.shared;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author 6266215
 */
public abstract class IServerGame extends IGame {
    
    protected Deck deck;
    protected Hand dealer;
    
    public IServerGame()
    {
        synchronized(this) { 
            this.gameID = ++IGame.GameCount;
        }
        deck = new Deck();
        dealer = new Hand();
    }
    
    public abstract void registerPlayer(Socket socket);
    
    public abstract void dealCard(int playerID) throws IOException;
    
    public abstract void checkHand(int playerID, Hand h, int clientPts) 
            throws IOException;
    
    public abstract void playerWin(int playerID, boolean twentyOne) 
            throws IOException;
    
    public abstract void dealerWin(int playerID) throws IOException;
    
    public abstract void stop();
}
