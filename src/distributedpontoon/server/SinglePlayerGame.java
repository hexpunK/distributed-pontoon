package distributedpontoon.server;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author 6266215
 */
public class SinglePlayerGame extends IServerGame
{   
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    public SinglePlayerGame()
    {
        super();
        this.socket = null;
        this.input = null;
        this.output = null;
    }
    
    @Override
    public void registerPlayer(Socket socket)
    {        
        this.socket = socket;
        
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ioEx) {
            gameError("GAME %d: Could not get socket streams. "
                    + "Reason:\n\t%s", ioEx.getMessage());
            return;
        }
        
        try {            
            output.writeObject(MessageType.JOIN_ACKNOWLEDGE);
            output.writeInt(1);
            output.writeInt(gameID);
            output.flush();
            gameMessage("Registered player.");
        } catch (IOException ioEx) {
            gameError("GAME %d: Failed to register player. "
                    + "Reason:\n\t", ioEx.getMessage());
        }
    }
    
    @Override
    public void dealCard() throws IOException
    {
        try {
            Card c = deck.pullCard();
            output.writeObject(MessageType.CARD_TRANSFER);
            output.writeObject(c);
            output.flush();
        } catch (Deck.DeckException deckEx) {
            gameMessage("Deck emptied!");
        }
    }

    @Override
    public void checkHand(int playerID, Hand h) throws IOException
    {
        
    }
    
    @Override
    public void stop()
    {
        gameMessage("Stopping game.");
        try {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            socket.close();
            gameMessage("Connection closed.");
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void run()
    {
        if (socket == null || socket.isClosed()) {
            gameMessage("No player registered for this game.");
            return;
        }
        
        MessageType reply;
        
        try {
            output.writeObject(MessageType.GAME_INITIALISE);
            try {
                output.writeObject(deck.pullCard());
                dealer.addCard(deck.pullCard());
                output.writeObject(deck.pullCard());
                dealer.addCard(deck.pullCard());
                output.flush();
            } catch (Deck.DeckException deckEx) {
                System.err.println(deckEx.getMessage());
            }
            
            while (!socket.isClosed()) {
                output.writeObject(MessageType.TURN_NOTIFY);
                output.flush();
                
                if (input == null) {
                    try {
                        input = new ObjectInputStream(socket.getInputStream());
                    } catch (IOException ex) { return; }
                }
                
                try {
                    reply = (MessageType)input.readObject();
                } catch (IOException noMsg) {
                    continue;
                }
                
                switch (reply) {
                    case TURN_RESPONSE:
                        char action = input.readChar();
                        switch (action) {
                            case 's':
                                gameMessage("Player has stuck.");
                                Hand h = (Hand)input.readObject();
                                int clientPts = input.readInt();
                                checkHand(1, h);
                                break;
                            case 't':
                                gameMessage("Player has twisted.");
                                dealCard();
                                break;
                            case 'b':
                                gameMessage("Player has bust.");
                                output.writeObject(MessageType.GAME_RESULT);
                                output.writeObject(false);
                                stop();
                                break;
                            default:
                                gameError("Unknown action sent to "
                                + "game:\n\t%s", reply);    
                        }
                        break;
                    case CLIENT_DISCONNECT:
                        gameMessage("Player leaving.");
                        stop();
                        break;
                    default:
                        gameError("Unknown message sent to "
                                + "game:\n\t%s", reply);
                }
            }
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } catch (ClassNotFoundException cnfEx) {
            System.err.println(cnfEx.getMessage());
        }
    }
}
