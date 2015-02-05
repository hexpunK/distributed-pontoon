package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.NetMessage;
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
public class Game implements IClientGame
{    
    private final int port;
    private final String serverName;
    private Socket connection;
    private final Hand hand;
    private final IPlayer player;
    private int bet;
    
    public Game(IPlayer player, int bet)
    {
        this.port = 50000;
        this.serverName = "UNCONNECTED";
        this.connection = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public Game(IPlayer player, int bet, String hostName)
    {
        this.port = 50000;
        this.serverName = hostName;
        this.connection = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
    public Game(IPlayer player, int bet, String hostName, int port)
    {
        this.port = port;
        this.serverName = hostName;
        this.connection = null;
        this.hand = new Hand();
        this.player = player;
        this.bet = bet;
    }
    
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
        try {
            connection.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void twist()
    {
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
            NetMessage<?> msg 
                    = new NetMessage<>(MessageType.TURN_RESPONSE, 't');
            output.writeObject(msg);
            output.flush();
            
            msg = (NetMessage)input.readObject();
            if (msg.Type != MessageType.CARD_TRANSFER) {
                System.err.println("ERROR");
                return;
            }
            Card c = (Card) msg.Contents;
            hand.addCard(c);
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
        }
    }
    
    @Override
    public void stand()
    {
        ObjectOutputStream output = null;
        
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            
            NetMessage<Character> msg 
                    = new NetMessage<>(MessageType.TURN_RESPONSE, 's');
            Card[] cards = new Card[hand.size()];
            cards = hand.getCards().toArray(cards);
            NetMessage<Card[]> cardMsg 
                    = new NetMessage<>(MessageType.TURN_RESPONSE, cards);
            NetMessage<Integer> pointMsg 
                    = new NetMessage<>(MessageType.TURN_RESPONSE, hand.total());
            output.writeObject(msg);
            output.writeObject(cardMsg);
            output.flush();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
        }
    }
    
    @Override
    public void bust()
    {
        ObjectOutputStream output = null;
        
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            NetMessage<Character> msg 
                    = new NetMessage<>(MessageType.TURN_RESPONSE, 'b');
            output.writeObject(msg);
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
        }
    }
    
    @Override
    public void run()
    {
        if (!connect()) return; // If connecting fails, just return.
        
        ObjectInputStream input;
        NetMessage<?> msg;
        
        try {
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            return;
        }
            
        while (connection.isConnected())
        {
            try {
                msg = (NetMessage)input.readObject();
                
                switch (msg.Type) {
                    case CARD_TRANSFER:
                        Card card = (Card)msg.Contents;
                        hand.addCard(card);
                        break;
                    case TURN_NOTIFY:
                        player.play(this);
                        break;
                    case GAME_RESULT:
                        if ((Boolean)msg.Contents)
                            player.adjustBalance(bet);
                        else
                            player.adjustBalance(-bet);
                        disconnect();
                        break;
                    default:
                        System.err.printf("Clients do not handle this type of "
                                + "message (%s)\n", msg.Type);
                }
            } catch (ClassNotFoundException cnfEx) {
                System.err.println(cnfEx.getMessage());
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            } finally {
                try {
                    input.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
        }
    }
}