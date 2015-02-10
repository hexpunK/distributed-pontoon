package distributedpontoon.shared;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.Card;

/**
 * A generalised base for the client side components of an {@link IGame}. This 
 * class exposes the basic functions that should be needed by a user interface 
 * implementation for a {@link IPlayer}.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
 */
public abstract class IClientGame extends IGame {
    
    /**
     * Sets the current bet to use in this {@link IClientGame}, the provided 
     * bet should be a positive number.
     * 
     * @param bet The new amount to use for a bet as an int.
     * @throws IllegalArgumentException Thrown if the provided bet is not a 
     * positive number.
     * @since 1.0
     */
    public abstract void setBet(int bet) throws IllegalArgumentException;
    
    /**
     * Gets the current bet assigned for this {@link IClientGame}. This number 
     * should never be equal to or lower than zero.
     * 
     * @return Returns the current bet amount as an int.
     * @since 1.0
     */
    public abstract int getBet();
    
    /**
     * Gets the current {@link Hand} for this game.
     * 
     * @return The hand as an {@link Hand} object.
     * @since 1.0
     */
    public abstract Hand getHand();
    
    /**
     * Checks to see whether this {@link IClientGame} is still connected to the 
     * remote server game.
     * 
     * @return Returns true if this {@link IClientGame} is still connected, 
     * false otherwise.
     * @since 1.0
     */
    public abstract boolean isConnected();
    
    /**
     * Connects to the specified server and port combination stored in the 
     * implementation of this {@link IClientGame}.
     * 
     * @return Returns true if the connection is successful, false if it fails 
     * for any reason.
     * @since 1.0
     */
    public abstract boolean connect();
    
    /**
     * Disconnects from the remote server.
     * 
     * @since 1.0
     */
    public abstract void disconnect();
    
    /**
     * Called when the current {@link IPlayer} needs an instruction from the 
     * server currently being played against.
     * 
     * @since 1.0
     */
    public abstract void ready();
    
    /**
     * Called when the current {@link IPlayer} wants a new {@link Card} from 
     * the server.
     * 
     * @since 1.0
     */
    public abstract void twist();
    
    /**
     * Called when the current {@link IPlayer} wants to inform the server that 
     * it doesn't want to receive any more {@link Card}s.
     * 
     * @since 1.0
     */
    public abstract void stand();
    
    /**
     * Called when the current {@link IPlayer} has a hand value greater than 21 
     * to tell the server that the game has ended.
     * 
     * @since 1.0
     */
    public abstract void bust();
}
