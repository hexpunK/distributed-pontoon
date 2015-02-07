package distributedpontoon.shared;

import distributedpontoon.client.IPlayer;

/**
 *
 * @author 6266215
 * @version 1.2
 * @since 2015-02-07
 */
public abstract class IGame implements Runnable {
    
    /**
     * Passed from the server to the client when the players hand wins.
     * 
     * @since 1.2
     */
    public static final boolean PLAYER_WIN = true;
    /**
     * Passed from the server to the client when the dealers hand wins.
     * 
     * @since 1.2
     */
    public static final boolean DEALER_WIN = false;
    
    /**
     * A number of actions that an {@link IPlayer} can send to a 
     * {@link IServerGame}.
     * 
     * @since 1.2
     */
    protected static enum PlayerAction {
        PLAYER_TWIST,
        PLAYER_STICK,
        PLAYER_BUST;
    }
    
    /** The total number of games that have run. */
    protected static volatile int GameCount;
    /** A unique ID for this {@link IGame} instance. */
    protected int gameID;
    
    /**
     * Prints information messages to the current {@link System#out} output 
     * stream. Prepends {@link IGame#gameID} to the message to improve the 
     * logging capability.
     * 
     * @param msg The message to print as a String. Accepts formatting 
     * parameters similarly to {@link String#format(java.lang.String, 
     * java.lang.Object...)}.
     * @param args Any number of objects to print in the resulting message. For 
     * objects to print useful data it may require overriding the {@link 
     * Object#toString()} method.
     * @since 1.1
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    public void gameMessage(String msg, Object...args)
    {
        String gameIDStr = gameID <= 0 ? "?" : String.valueOf(gameID);
        msg = String.format(msg, args);
        System.out.printf("GAME %s: %s\n", gameIDStr, msg);
    }
    
    /**
     * Prints information messages to the current {@link System#err} output 
     * stream. Prepends {@link IGame#gameID} to the message to improve the 
     * logging capability.
     * 
     * @param msg The message to print as a String. Accepts formatting 
     * parameters similarly to {@link String#format(java.lang.String, 
     * java.lang.Object...)}.
     * @param args Any number of objects to print in the resulting message. For 
     * objects to print useful data it may require overriding the {@link 
     * Object#toString()} method.
     * @since 1.1
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    public void gameError(String msg, Object...args)
    {
        String gameIDStr = gameID <= 0 ? "?" : String.valueOf(gameID);
        msg = String.format(msg, args);
        System.err.printf("GAME %s: %s\n", gameIDStr, msg);
    }
}
