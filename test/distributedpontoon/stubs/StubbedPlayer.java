package distributedpontoon.stubs;

import distributedpontoon.client.Game;
import distributedpontoon.client.IPlayer;

/**
 *
 * @author Jordan
 */
public class StubbedPlayer implements IPlayer {

    private int balance;
    
    public StubbedPlayer()
    {
        this.balance = 500;
    }
    
    @Override
    public void reigsterGame(Game game) { }

    @Override
    public void startGame() { }

    @Override
    public void play(Game caller) { }

    @Override
    public void setBalance(int bal) { this.balance = 500; }

    @Override
    public boolean adjustBalance(int deltaBal) 
    { 
        balance += deltaBal;
        return balance > 0;
    }

    @Override
    public int getBalance() { return 500; }
    
    @Override
    public void leaveGame(Game game) { }
}
