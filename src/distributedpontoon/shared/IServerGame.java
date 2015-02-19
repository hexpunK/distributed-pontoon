package distributedpontoon.shared;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.Card;
import java.io.IOException;
import java.net.Socket;

/**
 * Represents a game of Pontoon from the dealers perspective. This will run on 
 * a server and have clients connect to play against the dealer. Specific 
 * implementations of this class could allow for multiple players against a 
 * single dealer, or a single player against a single dealer for example.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-18
 */
public abstract class IServerGame extends IGame
{
    /** The {@link Deck} a dealer will draw from for this game. */
    protected Deck deck;
    /** The {@link Hand} for this dealer. */
    protected Hand dealer;
    
    /**
     * Creates a new {@link IServerGame}, providing a new unique ID for the game
     *  and creating a filled {@link Deck} for play.
     * 
     * @since 1.0
     */
    public IServerGame()
    {
        synchronized(this) { 
            this.gameID = ++IGame.GameCount;
        }
        deck = new Deck();
        dealer = new Hand();
    }
    
    /**
     * Registers a new {@link IPlayer} to this {@link IServerGame} using the 
     * {@link Socket} the player connects with. Implementations of this class 
     * will need to perform different actions when registering a player so this 
     * is left abstract.
     * 
     * @param socket The {@link Socket} the player has connected with.
     * @since 1.0
     */
    public abstract void registerPlayer(Socket socket);
    
    /**
     * Deals a {@link Card} to the players of this {@link IServerGame}. If 
     * multiple players exist, the player ID can be sent to this method to let 
     * the server find the correct player.
     * 
     * @param playerID The unique ID of the player to deal a card to.
     * @throws IOException Thrown if there are any issues communicating with the
     *  {@link IPlayer}.
     * @since 1.0
     */
    public abstract void dealCard(int playerID) throws IOException;
    
    /**
     * Checks the hand of the {@link IPlayer} that has been passed to this 
     * method.
     * 
     * @param playerID The unique ID of the player to send the results to.
     * @param h The {@link Hand} to inspect.
     * @throws IOException Thrown if there are any issues communicating with the
     *  {@link IPlayer}.
     * @since 1.1
     */
    public abstract void checkHand(int playerID, Hand h) 
            throws IOException;
    
    /**
     * Lets the dealer for this {@link IServerGame} take their turn. The 
     * method used for the dealer to draw cards is implementation dependant.
     * 
     * @param plyScore The score of the player to compare against if needed.
     * @return Returns true if the dealers {@link Hand} score is 21 or lower, 
     * false otherwise.
     * @since 1.2
     */
    public abstract boolean dealerPlay(int plyScore);
    
    /**
     * Sends a message to the {@link IPlayer} specified telling them that they 
     * won the hand. Allows the game to send a boolean to affect the number of 
     * credits returned.
     * 
     * @param playerID The unique ID of the player to send the message to.
     * @param twentyOne Set this to true if the player won with a Pontoon/ 2 
     * card hand worth 21 points.
     * @throws IOException Thrown if there are any issues communicating with the
     *  {@link IPlayer}.
     * @since 1.1
     */
    public abstract void playerWin(int playerID, boolean twentyOne) 
            throws IOException;
    
    /**
     * Sends a message to the {@link IPlayer} specified telling them that the 
     * dealer for this {@link IServerGame} has won the hand.
     * 
     * @param playerID The unique ID of the player to send the message to.
     * @throws IOException Thrown if there are any issues communicating with the
     *  {@link IPlayer}.
     * @since 1.0
     */
    public abstract void dealerWin(int playerID) throws IOException;
    
    /**
     * Stops this {@link IServerGame}.
     * 
     * @since 1.0
     */
    public abstract void stop();
}
