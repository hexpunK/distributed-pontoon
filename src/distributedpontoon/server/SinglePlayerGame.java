package distributedpontoon.server;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Deck.DeckException;
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
    public void dealCard(int playerID) throws IOException
    {
        try {
            Card c = deck.pullCard();
            output.writeObject(MessageType.CARD_TRANSFER);
            output.writeObject(c);
            output.flush();
        } catch (DeckException deckEx) {
            gameMessage("Deck emptied!");
        }
    }

    @Override
    public void checkHand(int playerID, Hand h, int clientPts) 
            throws IOException
    {
        int plyTotal = h.total();
        if (plyTotal != clientPts) {
            gameError("Player lied about their score.");
        }
        
        gameMessage("Player hand:\n%s", h);
        
        boolean plyHas21 = (plyTotal == 21);
        boolean plyHas5Card = (plyHas21 && h.size() == 5);
        boolean plyHas2Card = (plyHas21 && h.size() == 2);
        
        if (!dealerPlay(plyTotal)) {
            gameMessage("Dealer has bust with a score of %d!", dealer.total());
            playerWin(playerID, plyHas2Card);
            return;
        }
        int dlrTotal = dealer.total();
        
        boolean dlrHas21 = (dlrTotal == 21);
        boolean dlrHas5Card = (dlrHas21 && dealer.size() == 5);
        boolean dlrHas2Card = (dlrHas21 && dealer.size() == 2);
        
        if (plyHas2Card) {
            if (dlrHas2Card) {
                gameMessage("Player and Dealer have a Pontoon! Push.");
                playerWin(playerID, false);
            } else {
                gameMessage("Player wins with a Pontoon!");
                playerWin(playerID, true);
            }
        } else if (plyHas5Card) {
            if (dlrHas2Card) {
                gameMessage("Dealer wins with a 5-card trick!");
                dealerWin(playerID);
            } else if (dlrHas5Card) {
                gameMessage("Player and Dealer have a 5-card trick! Push.");
                playerWin(playerID, false);
            }else {
                gameMessage("Player wins with a 5-card trick!");
                playerWin(playerID, false);
            }
        } else {
            if (plyTotal > dlrTotal) {
                gameMessage("Player wins hand! Player: %d\tDealer: %d", 
                    plyTotal, dlrTotal);
                playerWin(playerID, false);
            } else {
                gameMessage("Dealer wins hand! Player: %d\tDealer: %d", 
                    plyTotal, dlrTotal);
                dealerWin(playerID);
            }
        }
    }
    
    public boolean dealerPlay(int plyScore)
    {
        while (dealer.total() < plyScore) {
            int dlrTotal = dealer.total();
            for (Card c : dealer.getCards()) {
                if (c.Rank == CardRank.ACE) {
                    if (!c.isAceHigh() && (dlrTotal + 10) < plyScore) {
                        c.setAceHigh(true);
                    } else if ((dlrTotal - 10) <= 21) {
                        c.setAceHigh(false);
                    }
                }
            }
            
            try {
                dealer.addCard(deck.pullCard());
            } catch (DeckException ex) {
                gameMessage("Deck emptied.");
                break;
            }
        }
        
        gameMessage("Dealer hand:\n%s", dealer);
        return (dealer.total() <= 21);
    }
    
    @Override
    public void playerWin(int playerID, boolean twentyOne) throws IOException
    {
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(PLAYER_WIN);
        output.writeBoolean(twentyOne);
        output.flush();
    }
    
    @Override
    public void dealerWin(int playerID) throws IOException
    {
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(DEALER_WIN);
        output.flush();
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
            gameError(ioEx.getMessage());
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
            while (!socket.isClosed()) {
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
                    case CLIENT_JOIN:
                        output.writeObject(MessageType.GAME_INITIALISE);
                        try {
                            output.writeObject(deck.pullCard());
                            dealer.addCard(deck.pullCard());
                            output.writeObject(deck.pullCard());
                            dealer.addCard(deck.pullCard());
                        } catch (Deck.DeckException deckEx) {
                            System.err.println(deckEx.getMessage());
                        } finally {
                            output.flush();
                        }
                        break;
                    case PLAYER_READY:
                        output.writeObject(MessageType.TURN_NOTIFY);
                        output.flush();
                        break;
                    case TURN_RESPONSE:
                        PlayerAction action = (PlayerAction)input.readObject();
                        switch (action) {
                            case PLAYER_STICK:
                                gameMessage("Player has stuck.");
                                Hand h = (Hand)input.readObject();
                                int clientPts = input.readInt();
                                checkHand(1, h, clientPts);
                                break;
                            case PLAYER_TWIST:
                                gameMessage("Player has twisted.");
                                dealCard(1);
                                break;
                            case PLAYER_BUST:
                                gameMessage("Player has bust.");
                                dealerWin(1);
                                break;
                            default:
                                gameError("Unknown action recieved: '%s'", 
                                        reply);    
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
