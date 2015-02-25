package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.IGame;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 * A client-side representation of a game of Pontoon. Stores the current {@link 
 * Hand} for an {@link IPlayer}, and handles the connection to the server-side 
 * version of this {@link IGame}. As a game of Pontoon is the player versus the 
 * dealer, this {@link ClientGame} doesn't need to be aware of the other {@link 
 * IPlayer}s in a game.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-16
 */
public class ClientGame extends IClientGame
{    
    /** The port to connect to. */
    private final int port;
    /** The host name or IP address to connect to. */
    private final String serverName;
    /** An {@link IPlayer} to request moves from for this {@link ClientGame}. */
    private final IPlayer player;
    /** The socket to connect to. */
    private Socket connection;
    /** The output stream to write to the server with. */
    private ObjectOutputStream output;
    /** The input stream to read messages from the server with. */
    private ObjectInputStream input;
    /** The {@link Hand} for the player. */
    private Hand hand;
    /** The {@link Hand} for the dealer. */
    private Hand dealerHand;
    /** The current bet for this {@link ClientGame} as an int. */
    private int bet;
    
    /**
     * Creates a new {@link ClientGame} that connects to a server running on 
     * the local machine (must be listening on port 55551).
     * 
     * @param player The {@link IPlayer} that will play this {@link ClientGame}.
     * @param bet The bet the {@link IPlayer} has made on this game.
     * @since 1.0
     */
    public ClientGame(IPlayer player, int bet)
    {
        this.gameID = -1;
        this.port = 55551;
        this.serverName = "localhost";
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.dealerHand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    /**
     * Creates a new {@link ClientGame} that connects to a server running on 
     * the specified host name/ IP address (must be listening on port 55551).
     * 
     * @param player The {@link IPlayer} that will play this {@link ClientGame}.
     * @param bet The bet the {@link IPlayer} has made on this game.
     * @param hostName The name/ IP address of the host to connect to as a 
     * String.
     * @since 1.0
     */
    public ClientGame(IPlayer player, int bet, String hostName)
    {
        this.gameID = -1;
        this.port = 50000;
        this.serverName = hostName;
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.dealerHand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    /**
     * Creates a new {@link ClientGame} that connects to a server running on 
     * the specified host name/ IP address, using the specified port number.
     * 
     * @param player The {@link IPlayer} that will play this {@link ClientGame}.
     * @param bet The bet the {@link IPlayer} has made on this game.
     * @param hostName The name/ IP address of the host to connect to as a 
     * String.
     * @param port The port to connect through on the remote machine as an int.
     * @throws IllegalArgumentException Thrown if the provided port number is 
     * not a valid TCP port.
     * @since 1.0
     */
    public ClientGame(IPlayer player, int bet, String hostName, int port) 
            throws IllegalArgumentException
    {
        if (port < 0 || port > 65536) {
            throw new IllegalArgumentException(
                    "Port number must be between 0 and 65536"
            );
        }
        
        this.gameID = -1;
        this.port = port;
        this.serverName = hostName;
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    /**
     * Sets the bet for the current {@link ClientGame}. If the new bet is 
     * greater than the players current balance, only the credits available will
     *  be used instead.
     * 
     * @param newBet The new bet to use as an int.
     * @throws IllegalArgumentException Thrown if the new bet is zero or lower.
     * @since 1.0
     */
    @Override
    public void setBet(int newBet) throws IllegalArgumentException
    {
        if (newBet <= 0) {
            throw new IllegalArgumentException(
                    "The provided bet cannot be zero or lower."
            );
        }
        // Ensure the player cannot bet more than they have.
        newBet = Math.min(newBet, player.getBalance());
        bet = newBet;
    }
    
    /**
     * Gets the current bet the playing {@link IPlayer} has placed for this 
     * game.
     * 
     * @return The current bet as an int.
     * @since 1.0
     */
    @Override
    public int getBet() { return bet; }

    /**
     * Gets the {@link Hand} for the playing {@link IPlayer}.
     * 
     * @return A {@link Hand} object that may contain no {@link Card}s.
     * @since 1.0
     */
    @Override
    public synchronized Hand getHand() { return hand; }
    
    /**
     * Gets the {@link Hand} from the dealer. This will be empty until the game 
     * has ended due to the dealers hand being unknown to players.
     * 
     * @return A {@link Hand} object that may be empty.
     * @since 1.2
     */
    @Override
    public synchronized Hand getDealerHand() { return dealerHand; }
    
    /**
     * Checks to see if this {@link ClientGame} is still connected to a remote 
     * game.
     * 
     * @return Returns true if this {@link ClientGame} is still connected, false
     *  otherwise.
     * @since 1.0
     */
    @Override
    public boolean isConnected()
    {
        if (connection == null) return false;
        return connection.isConnected();
    }
    
    /**
     * Attempts to connect to a remote game specified by the host name and port 
     * set for this {@link ClientGame}.
     * 
     * @return Returns true if the connection can be established, false 
     * otherwise.
     * @since 1.0
     */
    @Override
    public boolean connect()
    {
        gameMessage(Level.FINER, "Attempting to connect to game...");
        try {
            InetAddress address = InetAddress.getByName(serverName);
            connection = new Socket(address, port);
            output = new ObjectOutputStream(connection.getOutputStream());
            if (gameID < 0) // Tell the server what kind of game this is.
                output.writeObject(MessageType.CLIENT_JOIN_SP);
            else {
                output.writeObject(MessageType.CLIENT_JOIN_MP);
                output.writeInt(gameID);
            }
            output.flush();
        } catch (UnknownHostException hostEx) {
            gameError(hostEx.getMessage());
            return false;
        } catch (IOException ioEx) {
            gameError(ioEx.getMessage());
            return false;
        }        
        return true;
    }
    
    /**
     * Disconnects from the remote game.
     * 
     * @since 1.0
     */
    @Override
    public void disconnect()
    {        
        gameMessage(Level.FINER, "Disconnecting from game.");
        if (!connection.isClosed()) {
            try {
                output.writeObject(MessageType.CLIENT_DISCONNECT);
                output.flush(); 
            } catch (IOException ioEx) {
                gameMessage(Level.FINER, "Server has already closed this "
                        + "connection.");
            }
        }
        try { 
            if (output != null)
                output.close();
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (IOException closeEx) {
            gameMessage(Level.FINEST, 
                    "Failed to close connection safely. Reason:%n%s", 
                    closeEx.getMessage());
        }
        player.leaveGame(this);
        this.gameID = -1;
    }
    
    /**
     * Starts running the game by telling the server that the {@link IPlayer} is
     *  ready through sending a {@link MessageType#CLIENT_READY} message.
     * 
     * @since 1.1
     */
    @Override
    public void startGame()
    {
        hand = new Hand();
        player.adjustBalance(-bet);
        try {
            output.writeObject(MessageType.CLIENT_READY);
            output.writeInt(bet);
            output.flush();
        } catch (IOException ex) {
            gameError("Error starting game:%n%s", ex.getMessage());
        }
    }

    /**
     * Tells the server that the {@link IPlayer} playing this game is ready to 
     * take their turn.
     * 
     * @since 1.0
     */
    @Override
    public void ready()
    {
        /* Tell the server that this player is ready. */
        if (!connection.isClosed()) {
            try {
                output.writeObject(MessageType.PLAYER_READY);
                output.flush();
            } catch (IOException ioEx) {
                gameError(ioEx.getMessage());
            }
        }
    }
    
    /**
     * Tells the server that the {@link IPlayer} wants another {@link Card}.
     * 
     * @since 1.1
     */
    @Override
    public void twist()
    {   
        try {
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeObject(PlayerAction.PLAYER_TWIST);
            output.flush();
        } catch (IOException ioEx) {
            gameError(ioEx.getMessage());
        }
    }
    
    /**
     * Adds the specified {@link Card} to the {@link Hand} for the {@link 
     * IPlayer}. Automatically works out the soft total for the {@link Hand} and
     *  adjusts and aces to prevent the player going bust. Should the player go 
     * bust this will automatically call {@link ClientGame#bust()}.
     * 
     * @param card The new {@link Card} to add to the {@link Hand} for the 
     * player.
     * @since 1.1
     */
    protected void acceptCard(Card card)
    {
        synchronized(this) {
            hand.addCard(card);
        }
        gameMessage(Level.FINER, "Adding card %s.%nHand total %d.", 
                card, hand.total());
        for (Card c : hand.getCards()) {
            if ((c.Rank == Card.CardRank.ACE)
                && c.isAceHigh() 
                && (hand.total() > 21)) {
                    gameMessage(Level.FINER, "Soft total is bust (%d)", 
                            hand.total());
                    c.setAceHigh(false);
            }
        }
        
        if (hand.total() > 21) {
            gameMessage(Level.INFO, "Hard total is bust (%d).", hand.total());
            bust();
        }
    }
    
    /**
     * Tells the server that the {@link IPlayer} doesn't want to take any more 
     * turns.
     * 
     * @since 1.1
     */
    @Override
    public void stand()
    {
        try {    
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeObject(PlayerAction.PLAYER_STICK);
            output.writeObject(hand);
            output.flush();
        } catch (IOException ex) {
            gameError(ex.getMessage());
        }
    }
    
    /**
     * Tells the server that the {@link IPlayer} has gone bust.
     * 
     * @since 1.1
     */
    @Override
    public void bust()
    {   
        try {
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeObject(PlayerAction.PLAYER_BUST);
            output.writeObject(hand);
            output.flush();
        } catch (IOException ioEx) {
            gameError(ioEx.getMessage());
        }
    }
    
    /**
     * Listens for instructions from the remote {@link IServerGame} that this 
     * {@link IClientGame} is connected to.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        if (!connect()) {
            gameError("Could not connect to game.");
            return;
        } // If connecting fails, just return.
        
        MessageType msg;
         
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException ioEx) {
            gameError("Couldn't get input stream. Reason:%n%s", 
                    ioEx.getMessage());
            disconnect();
            return;
        }
        
        while (connection != null && !connection.isClosed())
        {
            try {
                try {
                    msg = (MessageType)input.readObject();
                } catch (IOException ex) {
                    disconnect();
                    return;
                }
                
                switch (msg) {
                    case JOIN_ACKNOWLEDGE:
                        // Set the player and game ID values.
                        player.setPlayerID(this, input.readInt());
                        gameID = input.readInt();
                        gameMessage("Connected!");
                        break;
                    case GAME_INITIALISE:
                        // Accept the first two cards the dealer sends.
                        Card cardOne = (Card)input.readObject();
                        Card cardTwo = (Card)input.readObject();
                        hand.addCard(cardOne);
                        hand.addCard(cardTwo);
                        ready();
                        break;
                    case CARD_TRANSFER:
                        // Accept cards dealt from the dealer.
                        Card card = (Card)input.readObject();
                        acceptCard(card);
                        ready();
                        break;
                    case TURN_NOTIFY:
                        // Tell the player to make a move.
                        player.play(this);
                        break;
                    case GAME_RESULT:
                        // Give the player their winnings and end the game.
                        gameMessage(Level.FINE, "Game over!");
                        boolean winner = input.readBoolean();
                        dealerHand = (Hand)input.readObject();
                        gameMessage(Level.FINE, "Player hand:%n%s", hand);
                        gameMessage(Level.FINE, "Dealer hand:%n%s", dealerHand);
                        if (winner == PLAYER_WIN) {
                            boolean pontoon = input.readBoolean();
                            if (pontoon) {
                                player.adjustBalance((int)(bet*1.5f));
                            } else {
                                player.adjustBalance(bet);
                            }
                            player.playerWin(this, pontoon);
                        } else {
                            player.dealerWin(this);
                        }
                        disconnect();
                        break;
                    default:
                        gameError("Clients do not handle this type of "
                                + "message (%s)%n", msg);
                }
            } catch (IOException | ClassNotFoundException ioEx) {
                gameError("Couldn't read server message. Reason:%n%s", 
                        ioEx.getMessage());
            }
        }
    }

    /**
     * Gets some details about this {@link ClientGame} and returns them in a 
     * {@link String}.
     * 
     * @return A String containing details about this {@link ClientGame}.
     * @since 1.2
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        return String.format("Client-side game - %d (Connected: %s)", 
            gameID, (isConnected() ? "YES" : "NO"));
    }
}