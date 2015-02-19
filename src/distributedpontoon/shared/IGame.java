package distributedpontoon.shared;

import distributedpontoon.client.IPlayer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic base for a game of cards. Tracks the current game ID and the total 
 * number of games that have run.
 * 
 * @author 6266215
 * @version 1.4
 * @since 2015-02-19
 */
public abstract class IGame implements Runnable {
    
    /** Provides logging for all the games run. */
    protected static final Logger logger = 
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        
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
        /** Sent to the server when the {@link IPlayer} wants a new card. */
        PLAYER_TWIST,
        /** Sent to the server when the {@link IPlayer} wants no new cards. */
        PLAYER_STICK,
        /** Sent to the server when the {@link IPlayer} goes over 21 points. */
        PLAYER_BUST;
    }
    
    /** The total number of games that have run. */
    protected static volatile int GameCount;
    /** A unique ID for this {@link IGame} instance. */
    protected int gameID;
    
    /**
     * Gets the unique ID for this {@link IGame}.
     * 
     * @return The game ID as an int.
     * @since 1.3
     */
    public int getGameID() { return gameID; }
    
    /**
     * Gets the total number of {@link IGame}s played.
     * 
     * @return The total number of games as an int.
     * @since 1.3
     */
    public static int TotalGames() { return GameCount; }
    
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
    public final synchronized void gameMessage(String msg, Object...args)
    {
        String gameIDStr = gameID <= 0 ? "?" : String.valueOf(gameID);
        msg = String.format(msg, args);
        logger.log(Level.INFO, "GAME {0} : {1}", new Object[] {gameIDStr, msg});
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
    public final synchronized void gameError(String msg, Object...args)
    {
        String gameIDStr = gameID <= 0 ? "?" : String.valueOf(gameID);
        msg = String.format(msg, args);
        logger.log(Level.WARNING, "GAME {0} : {1}", 
                new Object[] {gameIDStr, msg});
    }
}
