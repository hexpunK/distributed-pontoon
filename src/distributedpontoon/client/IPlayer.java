package distributedpontoon.client;

import distributedpontoon.shared.NetMessage.MessageType;

/**
 * Base class for Pontoon players, contains all the required methods a player 
 * would need to call.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
 */
public interface IPlayer
{   
    /**
     * Sets the ID this {@link IPlayer} will use to identify themselves to 
     * a specific game.
     * 
     * @param game The {@link Game} this ID is tied to.
     * @param id The unique ID for the game as an int.
     * @since 1.0
     */
    public void setPlayerID(Game game, int id);
    
    /**
     * Registers a game to this {@link IPlayer}. Can be used to register single 
     * games or multiple times (with multiple calls).
     * 
     * @param game The new {@link Game} to link to this {@link IPlayer}.
     * @since 1.0
     */
    public void reigsterGame(Game game);
    
    /**
     * Starts playing the {@link Game} instances bound to this {@link IPlayer}.
     * 
     * @since 1.0
     */
    public void startGame();
    
    /**
     * Called by {@link Game} instances to get the player to perform an action.
     * {@link Game} instances will call this when they receive a 
     * {@link MessageType#TURN_NOTIFY} message.
     * 
     * @param caller The {@link Game} object that called this method.
     * @since 1.0
     */
    public void play(Game caller);
    
    /**
     * Sets the amount of credits this {@link IPlayer} has to bet with. 
     * Different implementations of this interface may perform more operations 
     * on the provided value.
     * 
     * @param bal The new amount of credits as an int.
     * @since 1.0
     */
    public void setBalance(int bal);
    
    /**
     * Changes the amount of credits this {@link IPlayer} has to bet with by
     *  the specified amount. To deduct credits use a negative value. This 
     * method will also check if the player has any credits left and returns 
     * true if there is at least one credit.
     * 
     * @param deltaBal The amount to adjust the player credits by as an int.
     * @return Returns true if the player still has a positive balance, false 
     * otherwise.
     * @since 1.0
     */
    public boolean adjustBalance(int deltaBal);
    
    /**
     * Gets the current amount of credits this {@link IPlayer} has left to play 
     * with.
     * 
     * @return The number of credits to play with as an int.
     * @since 1.0
     */
    public int getBalance();
    
    /**
     * Disconnects this {@link IPlayer} from the specified {@link Game} 
     * safely. If multiple games are assigned to the player, the others should 
     * be untouched when this is called.
     * 
     * @param game The {@link Game} to stop taking part in.
     * @since 1.0
     */
    public void leaveGame(Game game);
}
