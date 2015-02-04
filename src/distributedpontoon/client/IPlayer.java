package distributedpontoon.client;

/**
 *
 * @author 6266215
 */
public interface IPlayer
{    
    public void reigsterGame();
    
    public void startGame();
    
    public void play(Game caller);
    
    public void setBalance(int bal);
    
    public boolean adjustBalance(int deltaBal);
    
    public int getBalance();
}
