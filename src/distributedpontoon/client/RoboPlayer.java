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
    private boolean playing;
    
    public RoboPlayer()
    {
        this.randomiser = new Random();
        this.threshold = randomiser.nextInt(21);
        this.games = new HashMap<>();
        this.playerIDs = new HashMap<>();
    }
    
    @Override
    public void init()
    {
        String[] addresses = new String[]{"localhost", "localhost", "localhost"};
        for (String address : addresses) {
            IClientGame game = new ClientGame(this, 50, address, 50000);
            Thread t = new Thread(game);
            games.put(game, t);
            playerIDs.put(game, -1);
        }
        startGame();
        playing = true;
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
    public boolean isPlaying() { return playing; }
    
    @Override
    public void startGame()
    {
        System.out.printf("RoboPlayer with threshold %d started.\n", threshold);
        for (IClientGame g : games.keySet())
            games.get(g).start();
    }

    @Override
    public void play(IClientGame caller)
    {
        if (caller.getHand().total() < threshold) {
            caller.twist();
        } else {
            caller.stand();
        }
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
    public synchronized void leaveGame(IClientGame game)
    {
        game.disconnect();
        try {
            Thread t = games.remove(game);
            t.join(1000);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        
        if (games.isEmpty())
            playing = false;
    }
}
