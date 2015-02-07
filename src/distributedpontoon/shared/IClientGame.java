package distributedpontoon.shared;

/**
 *
 * @author 6266215
 */
public abstract class IClientGame extends IGame {
    
    public abstract void setBet(int bet) throws IllegalArgumentException;
    
    public abstract int getBet();
    
    public abstract Hand getHand();
    
    public abstract boolean isConnected();
    
    public abstract boolean connect();
    
    public abstract void disconnect();
    
    public abstract void ready();
    
    public abstract void twist();
    
    public abstract void stand();
    
    public abstract void bust();
}
