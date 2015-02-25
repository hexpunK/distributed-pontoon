package distributedpontoon.client;

import distributedpontoon.directoryservice.DirectoryService;
import distributedpontoon.server.Server;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Triple;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for Pontoon players, contains all the required methods a player 
 * would need to call.
 * 
 * @author 6266215
 * @version 1.3
 * @since 2015-02-09
 */
public abstract class IPlayer
{   
    /** The remaining credits this {@link IPlayer} can bet with.  */
    protected int balance;
    /** Indicates whether or not this {@link IPlayer} is still playing. */
    protected boolean playing;
    
    /**
     * Start this {@link IPlayer} instance and accept the initial input based on
     *  the implementation requirements (server selection, stats, etc.).
     * 
     * @since 1.1
     */
    public abstract void init();
    
    /**
     * Attempts to connect to the specified {@link DirectoryService} to let this
     *  {@link IPlayer} find any active {@link Server}s.
     * 
     * @return A {@link Set} of unique host name-port number {@link Triple}s. 
     * This can be empty.
     * @since 1.3
     */
    public Set<Triple<String, Integer, Integer>> findServers()
    {
        Set<Triple<String, Integer, Integer>> servers = new HashSet<>();
        String serverName = Client.DIR_HOSTNAME;
        int directoryPort = Client.DIR_PORT;
        Socket directorySocket;
        try {
            InetAddress address = InetAddress.getByName(serverName);
            directorySocket = new Socket(address, directoryPort);
            ObjectOutputStream output = 
                    new ObjectOutputStream(directorySocket.getOutputStream());
            
            output.writeObject(MessageType.QUERY_SERVERS);
            output.flush();
            ObjectInputStream input = 
                    new ObjectInputStream(directorySocket.getInputStream());
            MessageType reply = (MessageType)input.readObject();
            if (reply == MessageType.QUERY_SERVERS) {
                servers = (HashSet)input.readObject();
            }
        } catch (UnknownHostException hostEx) {
            System.err.printf("Directory server not found, host '%s' may not "
                    + "exist.", serverName);
            return null;
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Could not contact directory server. No servers"
                    + " found.");
            return null;
        }
        return servers;
    }
    
    /**
     * Sets the ID this {@link IPlayer} will use to identify themselves to 
     * a specific game.
     * 
     * @param game The {@link IClientGame} this ID is tied to.
     * @param id The unique ID for the game as an int.
     * @since 1.0
     */
    public abstract void setPlayerID(IClientGame game, int id);
    
    /**
     * Registers a game to this {@link IPlayer}. Can be used to register single 
     * games or multiple times (with multiple calls).
     * 
     * @param game The new {@link IClientGame} to link to this {@link IPlayer}.
     * @since 1.0
     */
    public abstract void reigsterGame(IClientGame game);
    
    /**
     * Checks to see if this {@link IPlayer} is in a game or not.
     * 
     * @return Returns true if this {@link IPlayer} is currently playing. False 
     * otherwise.
     * @since 1.2
     */
    public abstract boolean isPlaying();
    
    /**
     * Starts playing the {@link IClientGame} instances bound to this {@link 
     * IPlayer}.
     * 
     * @since 1.0
     */
    public abstract void startGame();
    
    /**
     * Called by {@link IClientGame} instances to get the player to perform an 
     * action. {@link IClientGame} instances will call this when they receive a 
     * {@link MessageType#TURN_NOTIFY} message.
     * 
     * @param caller The {@link IClientGame} object that called this method.
     * @since 1.0
     */
    public abstract void play(IClientGame caller);
    
    /**
     * Sets the amount of credits this {@link IPlayer} has to bet with.
     * 
     * @param bal The new amount of credits as an int.
     * @since 1.0
     */
    public synchronized void setBalance(int bal) { this.balance = bal; }
    
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
    public synchronized boolean adjustBalance(int deltaBal)
    {
        this.balance += deltaBal;
        return this.balance > 0;
    }
    
    /**
     * Gets the current amount of credits this {@link IPlayer} has left to play 
     * with.
     * 
     * @return The number of credits to play with as an int.
     * @since 1.0
     */
    public synchronized int getBalance()
    {
        return this.balance;
    }
    
    /**
     * Called when an {@link IPlayer} wins an {@link IClientGame}, specific 
     * {@link IPlayer} implementations can then display relevant messages to the
     *  user.
     * 
     * @param game The {@link IClientGame} that was won.
     * @param pontoon If the {@link IPlayer} won with a pontoon, this will be 
     * true.
     * @since 1.3
     */
    public abstract void playerWin(IClientGame game, boolean pontoon);
    
    /**
     * Called when an {@link IPlayer} loses a {@link IClientGame} to the dealer.
     * 
     * @param game The {@link IClientGame} that was lost.
     * @since 1.3
     */
    public abstract void dealerWin(IClientGame game);
    
    /**
     * Disconnects this {@link IPlayer} from the specified {@link IClientGame} 
     * safely. If multiple games are assigned to the player, the others should 
     * be untouched when this is called.
     * 
     * @param game The {@link IClientGame} to stop taking part in.
     * @since 1.0
     */
    public abstract void leaveGame(IClientGame game);
}
