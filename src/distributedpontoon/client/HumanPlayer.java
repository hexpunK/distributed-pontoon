package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.NetMessage.MessageType;

/**
 * A base class for clients that will allow the user to play a game of Pontoon. 
 * To actually create a human player, an instance of {@link CLIPlayer} or 
 * {@link GUIPlayer} will be needed.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
 */
public abstract class HumanPlayer implements IPlayer
{
    /** The amount of money the player has left to bet with. */
    protected int balance;
    /** The game this player is involved in. Human players can only play one
     game at a time. */
    protected IClientGame game;
    /** A thread to run the game in to avoid locking up whilst the player 
     takes their turn. */
    protected Thread gameThread;
    /** A unique ID for this {@link HumanPlayer} to be referred to by. */
    protected int playerID;
    /** Keeps track of whether the playing is in a game or not. */
    private boolean playing;

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

    @Override
    public boolean isPlaying() { return playing; }

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
        playing = true;
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
     * Sets the amount of credits this {@link HumanPlayer} has to bet with.
     * 
     * @param bal The new amount of credits as an int.
     * @since 1.0
     */
    @Override
    public void setBalance(int bal) { balance = bal; }

    /**
     * Changes the amount of credits this {@link HumanPlayer} has to bet with by
     *  the specified amount. To deduct credits use a negative value. This 
     * method will also check if the player has any credits left and returns 
     * true if there is at least one credit.
     * 
     * @param deltaBal The amount to adjust the player credits by as an int.
     * @return Returns true if the player still has a positive balance, false 
     * otherwise.
     * @since 1.0
     */
    @Override
    public boolean adjustBalance(int deltaBal)
    { 
        balance += deltaBal;
        return balance > 0;
    }

    /**
     * Gets the current amount of credits this {@link HumanPlayer} has left to 
     * play with.
     * 
     * @return The number of credits to play with as an int.
     * @since 1.0
     */
    @Override
    public int getBalance() { return balance; }
    
    /**
     * Displays details on the users hand. Specific implementations of this 
     * class will need to handle this different to suit the user interface in 
     * use.
     * 
     * @since 1.0
     */
    public abstract void viewHand();
    
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
        this.game.disconnect();
        try {
            gameThread.join(1000);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        playing = false;
    }
}