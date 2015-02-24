package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.NetMessage.MessageType;

/**
 * A base class for clients that will allow the user to play a game of Pontoon. 
 * To actually create a human player, an instance of {@link CLIPlayer} or 
 * {@link GUIPlayer} will be needed.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-02-04
 */
public abstract class HumanPlayer extends IPlayer
{
    /** The current bet for this player. */
    protected int bet;
    /** The game this player is involved in. Human players can only play one
     game at a time. */
    protected IClientGame game;
    /** A thread to run the game in to avoid locking up whilst the player 
     takes their turn. */
    protected Thread gameThread;
    /** A unique ID for this {@link HumanPlayer} to be referred to by. */
    protected int playerID;

    /**
     * Creates a new {@link HumanPlayer} with a negative player ID, zero credits
     *  as a balance and no {@link IClientGame} assigned.
     * 
     * @since 1.0
     */
    public HumanPlayer()
    {
        this.game = null;
        this.gameThread = null;
        this.playerID = -1;
        this.balance = 0;
        this.bet = 0;
    }
    
    /**
     * Sets the unique ID for this {@link HumanPlayer}. The {@link IClientGame} 
     * instance provided has no impact on this as a {@link HumanPlayer} can only
     *  be playing one game at a time.
     * 
     * @param game The {@link IClientGame} that called this method.
     * @param id The unique ID as an int.
     * @since 1.0
     */
    @Override
    public void setPlayerID(IClientGame game, int id)
    {
        this.playerID = id;
    }
    
    /**
     * Assigns a game to this {@link HumanPlayer}, the game must not have 
     * started yet.
     * 
     * @param game The new {@link IClientGame} for this {@link HumanPlayer} to 
     * take part in.
     * @since 1.0
     */
    @Override
    public void reigsterGame(IClientGame game) 
    {
        this.game = game;
    }

    /**
     * Gets the current {@link IClientGame} being played by this {@link 
     * HumanPlayer}.
     * 
     * @return An {@link IClientGame} instance if a game is being played, null 
     * if no game is being played.
     * @since 1.1
     */
    public IClientGame getGame() { return this.game; }
    
    /**
     * Checks to see whether this {@link HumanPlayer} is still playing a game of
     *  Pontoon or not. This should only return false when the {@link IPlayer} 
     * has indicated that it doesn't want to play any more games.
     * 
     * @return Returns true if the {@link HumanPlayer} wants to continue playing
     *  , false otherwise.
     * @since 1.0
     */
    @Override
    public synchronized boolean isPlaying() { return playing; }

    /**
     * Initialises the {@link IClientGame} this player is part of. If the player
     *  isn't assigned to a game, nothing will happen.
     * 
     * @since 1.0
     */
    @Override
    public void startGame()
    {
        if (game == null) return;
        
        gameThread = new Thread(game);
        gameThread.start();
    }

    /**
     * Play will be called by a {@link IClientGame} when it receives a {@link 
     * MessageType#TURN_NOTIFY} message, what happens in this method is up to 
     * the specific implementations of this class.
     * 
     * @param caller The {@link IClientGame} that triggered this method call.
     * @since 1.0
     */
    @Override
    public abstract void play(IClientGame caller);
    
    /**
     * Disconnects this {@link HumanPlayer} from the specified {@link 
     * IClientGame} safely.
     * 
     * @param game The {@link IClientGame} to stop taking part in.
     * @since 1.0
     */
    @Override
    public void leaveGame(IClientGame game)
    {
        if (this.gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        playing = false;
    }
}