package distributedpontoon.stubs;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.IClientGame;

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
    public void reigsterGame(IClientGame game) { }

    @Override
    public void startGame() { }

    @Override
    public void play(IClientGame caller) { }

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
    public void leaveGame(IClientGame game) { }

    @Override
    public void setPlayerID(IClientGame game, int id) { }

    @Override
    public void init() { }
}
