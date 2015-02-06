package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author 6266215
 */
public class RoboPlayer implements IPlayer
{
    private final int threshold;
    private final Random randomiser;
    private final HashMap<IClientGame, Thread> games;
    private final HashMap<IClientGame, Integer> playerIDs;
    private int balance;
    
    public RoboPlayer()
    {
        this.randomiser = new Random();
        this.threshold = randomiser.nextInt(21);
        this.games = new HashMap<>();
        this.playerIDs = new HashMap<>();
    }

    @Override
    public void setPlayerID(IClientGame game, int id)
    {
        if (playerIDs.containsKey(game))
            playerIDs.put(game, id);
    }
    
    @Override
    public void reigsterGame(IClientGame game)
    {
        Thread gameThread = new Thread(game);
        games.put(game, gameThread);
    }

    @Override
    public void startGame()
    {
        for (IClientGame g : games.keySet())
        {
            Thread t = games.get(g);
            t.start();
        }
    }

    @Override
    public void play(IClientGame game)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public void setBalance(int bal) { this.balance = bal; }

    @Override
    public boolean adjustBalance(int deltaBal)
    {
        balance += deltaBal;
        return balance > 0;
    }

    @Override
    public int getBalance() { return balance; }
    
    @Override
    public void leaveGame(IClientGame game)
    {
        game.disconnect();
        try {
            games.get(game).join();
            games.remove(game);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
