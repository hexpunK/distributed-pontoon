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
public class Game extends IClientGame
{    
    private final int port;
    private final String serverName;
    private Socket connection;
    private ObjectOutputStream output;
    private final Hand hand;
    private final IPlayer player;
    private int bet;
    
    public Game(IPlayer player, int bet)
    {
        this.port = 50000;
        this.serverName = "localhost";
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public Game(IPlayer player, int bet, String hostName)
    {
        this.port = 50000;
        this.serverName = hostName;
        this.connection = null;
        this.output = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public Game(IPlayer player, int bet, String hostName, int port)
    {
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
        System.out.println("Attempt to connect to game...");
        try {
            InetAddress address = InetAddress.getByName(serverName);
            connection = new Socket(address, port);
        } catch (UnknownHostException hostEx) {
            System.err.println(hostEx.getMessage());
            return false;
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            return false;
        }        
        return true;
    }
    
    @Override
    public void disconnect()
    {
        System.out.println("Disconnecting from game.");
        if (output == null || connection == null)
            return; // Output or connection is already closed.
        
        try {
            output.writeObject(MessageType.CLIENT_DISCONNECT);
            output.flush(); 
            output.close();
            connection.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void twist()
    {   
        try {
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeChar('t');
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void stand()
    {
        try {    
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeChar('s');
            output.writeObject(hand);
            output.writeInt(hand.total());
            output.flush();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    @Override
    public void bust()
    {   
        try {
            output.writeObject(MessageType.TURN_RESPONSE);
            output.writeChar('b');
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void run()
    {
        if (!connect()) return; // If connecting fails, just return.
        System.out.println("Connected!");
        
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
                        break;
                    case GAME_INITIALISE:
                        // Accept the first two cards the dealer sends.
                        Card cardOne = (Card)input.readObject();
                        Card cardTwo = (Card)input.readObject();
                        hand.addCard(cardOne);
                        hand.addCard(cardTwo);
                        break;
                    case CARD_TRANSFER:
                        // Accept cards dealt from the dealer.
                        Card card = (Card)input.readObject();
                        hand.addCard(card);
                        break;
                    case TURN_NOTIFY:
                        // Tell the player to make a move.
                        player.play(this);
                        break;
                    case GAME_RESULT:
                        // Give the player their winnings and end the game.
                        if (input.readBoolean())
                            player.adjustBalance(bet);
                        disconnect();
                        break;
                    default:
                        System.err.printf("Clients do not handle this type of "
                                + "message (%s)\n", msg);
                }
            } catch (ClassNotFoundException cnfEx) {
                System.err.println(cnfEx.getMessage());
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            }
        }
        
        try {
            input.close();
        } catch (IOException innerIOEx) {
            System.err.println(innerIOEx.getMessage());
        }
    }
}