package distributedpontoon.shared;

/**
 *
 * @author 6266215
 */
public abstract class IGame implements Runnable {
    
    protected static volatile int GameCount;
    protected int gameID;
    
    public void gameMessage(String msg, Object...args)
    {
        msg = String.format(msg, args);
        System.out.printf("GAME %d: %s\n", gameID, msg);
    }
    
    public void gameError(String msg, Object...args)
    {
        msg = String.format(msg, args);
        System.err.printf("GAME %d: %s\n", gameID, msg);
    }
}
