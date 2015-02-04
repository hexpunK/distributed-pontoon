package distributedpontoon.shared;

/**
 *
 * @author Jordan
 */
public interface IClientGame extends Runnable {
    
    public Hand getHand();
    
    public boolean isConnected();
    
    public boolean connect();
    
    public void disconnect();
    
    public void twist();
    
    public void stand();
    
    public void bust();
    
    public void showHand();
}
