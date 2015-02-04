package distributedpontoon.client;

import distributedpontoon.shared.Hand;
import java.util.ArrayList;
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
    private Hand hand;
    private HashMap<Game, Thread> games;
    
    public RoboPlayer()
    {
        this.hand = new Hand();
        this.randomiser = new Random();
        this.balance = 0;
        this.threshold = randomiser.nextInt(21);
        this.games = new HashMap<>();
    }
    
    @Override
    public void reigsterGame()
    {
        Game newGame = new Game(this);
        Thread gameThread = new Thread(newGame);
        games.put(newGame, gameThread);
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
    public void play(Game caller) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public void setBalance(int bal) { this.balance = bal; }

    @Override
    public boolean adjustBalance(int deltaBal) {
        balance += deltaBal;
        return balance > 0;
    }

    @Override
    public int getBalance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
