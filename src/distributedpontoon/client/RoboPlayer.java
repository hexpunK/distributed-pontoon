package distributedpontoon.client;

import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author 6266215
 */
public class RoboPlayer implements IPlayer
{
    private int balance;
    private int threshold;
    private Random randomiser;
    private HashMap<Game, Thread> games;
    private HashMap<Game, Integer> playerIDs;
    
    public RoboPlayer()
    {
        this.randomiser = new Random();
        this.threshold = randomiser.nextInt(21);
        this.games = new HashMap<>();
        this.playerIDs = new HashMap<>();
    }

    @Override
    public void setPlayerID(Game game, int id)
    {
        if (playerIDs.containsKey(game))
            playerIDs.put(game, id);
    }
    
    @Override
    public void reigsterGame(Game game)
    {
        Thread gameThread = new Thread(game);
        games.put(game, gameThread);
    }

    @Override
    public void startGame()
    {
        for (Game g : games.keySet())
        {
            Thread t = games.get(g);
            t.start();
        }
    }

    @Override
    public void play(Game game)
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
    public int getBalance()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void leaveGame(Game game)
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
