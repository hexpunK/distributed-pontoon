package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author 6266215
 */
public class ClientGame extends IClientGame
{    
    private final int port;
    private final String serverName;
    private Socket connection;
    private ObjectOutputStream output;
    private final IPlayer player;
    private Hand hand;
    private int bet;
    
    public ClientGame(IPlayer player, int bet)
    {
        this.port = 50000;
        this.serverName = "localhost";
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public ClientGame(IPlayer player, int bet, String hostName)
    {
        this.port = 50000;
        this.serverName = hostName;
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public ClientGame(IPlayer player, int bet, String hostName, int port) 
            throws IllegalArgumentException
    {
        if (port < 0 || port > 65536) {
            throw new IllegalArgumentException(
                    "Port number must be between 0 and 65536"
            );
        }
        
        this.port = port;
        this.serverName = hostName;
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
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
    
    @Override
    public int getBet() { return bet; }
    
    @Override
    public Hand getHand() { return hand; }
    
    @Override
    public boolean isConnected()
    {
        if (connection == null) return false;
        return connection.isConnected();
    }
    
    @Override
    public boolean connect()
    {
        gameMessage("Attempt to connect to game...");
        try {
            InetAddress address = InetAddress.getByName(serverName);
            connection = new Socket(address, port);
        } catch (UnknownHostException hostEx) {
            gameError(hostEx.getMessage());
            return false;
        } catch (IOException ioEx) {
            gameError(ioEx.getMessage());
            return false;
        }        
        return true;
    }
    
    @Override
    public void disconnect()
    {
        gameMessage("Disconnecting from game.");
        if (output == null || connection == null)
            return; // Output or connection is already closed.
        
        try {
            output.writeObject(MessageType.CLIENT_DISCONNECT);
            output.flush(); 
            output.close();
            connection.close();
        } catch (IOException ioEx) {
            gameError(ioEx.getMessage());
        }
    }
    
    public void startGame()
    {
        hand = new Hand();
        try {
            output.writeObject(MessageType.CLIENT_JOIN);
            output.flush();
        } catch (IOException ex) {
            gameError("Error starting game:\n%s", ex.getMessage());
        }
    }

    @Override
    public void ready() {
        /* Tell the server that this {@link ClientGame} is ready. */
        if (!connection.isClosed()) {
            try {
                output.writeObject(MessageType.PLAYER_READY);
                output.flush();
            } catch (IOException ioEx) {
                gameError(ioEx.getMessage());
            }
        }
    }
    
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
    
    public void acceptCard(Card card)
    {
        hand.addCard(card);
        gameMessage("Adding card %s.\nHand total %d.", card, hand.total());
        for (Card c : hand.getCards()) {
            if ((c.Rank == Card.CardRank.ACE)
                && c.isAceHigh() 
                && (hand.total() > 21)) {
                    gameMessage("Soft total is bust (%d)", hand.total());
                    c.setAceHigh(false);
            }
        }
        
        if (hand.total() > 21) {
            gameMessage("Hard total is bust (%d).", hand.total());
            bust();
        }
    }
    
    @Override
    public void stand()
    {
        try {    
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeObject(PlayerAction.PLAYER_STICK);
            output.writeObject(hand);
            output.writeInt(hand.total());
            output.flush();
        } catch (IOException ex) {
            gameError(ex.getMessage());
        }
    }
    
    @Override
    public void bust()
    {   
        try {
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeObject(PlayerAction.PLAYER_BUST);
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void run()
    {
        if (!connect()) return; // If connecting fails, just return.
        
        ObjectInputStream input;
        MessageType msg;
         
        try {
            input = new ObjectInputStream(connection.getInputStream());
            output = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            return;
        }
        
        while (!connection.isClosed())
        {
            try {
                try {
                    msg = (MessageType)input.readObject();
                } catch (IOException ex) {
                    continue;
                }
                
                switch (msg) {
                    case JOIN_ACKNOWLEDGE:
                        // Set the player and game ID values.
                        player.setPlayerID(this, input.readInt());
                        gameID = input.readInt();
                        gameMessage("Connected!");
                        startGame();
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
                        gameMessage("Game over!");
                        boolean winner = input.readBoolean();
                        Hand dealerHand = (Hand)input.readObject();
                        if (winner == PLAYER_WIN) {
                            if (input.readBoolean()) {
                                gameMessage("Player won hand with a Pontoon!.");
                                player.adjustBalance((int)(bet*1.5f));
                            } else {
                                gameMessage("Player won hand.");
                            }
                        } else {
                            gameMessage("Dealer won hand.");
                            player.adjustBalance(-bet);
                        }
                        gameMessage("Player hand:\n%s", hand);
                        gameMessage("Dealer hand:\n%s", dealerHand);
                        player.leaveGame(this);
                        break;
                    default:
                        gameError("Clients do not handle this type of "
                                + "message (%s)\n", msg);
                }
            } catch (ClassNotFoundException cnfEx) {
                gameError(cnfEx.getMessage());
            } catch (IOException ioEx) {
                gameError(ioEx.getMessage());
            }
        }
        
        try {
            input.close();
        } catch (IOException innerIOEx) {
            System.err.println(innerIOEx.getMessage());
        }
    }
}