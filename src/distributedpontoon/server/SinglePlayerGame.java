package distributedpontoon.server;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.server.Deck.DeckException;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;

/**
 * An {@link IServerGame} implementation to handle a single {@link IPlayer} 
 * playing against a dealer.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-02-07
 */
public class SinglePlayerGame extends IServerGame
{   
    /** The {@link Socket} for the playing client. */
    private Socket socket;
    /** Input from the client socket. */
    private ObjectInputStream input;
    /** Output to the client socket. */
    private ObjectOutputStream output;
    /** The bet for the client playing. */
    private int bet;
    
    /**
     * Creates a new {@link SinglePlayerGame} with no connected socket. To use 
     * this {@link SinglePlayerGame} a player will need to be registered with 
     * {@link IServerGame#registerPlayer(java.net.Socket)}.
     * 
     * @since 1.0
     */
    public SinglePlayerGame()
    {
        super();
        this.socket = null;
        this.input = null;
        this.output = null;
        this.bet = 0;
    }
    
    /**
     * Registers a {@link IPlayer} with this {@link SinglePlayerGame}. The 
     * provided {@link Socket} is used to identify the new player.
     * 
     * @param socket The {@link Socket} used by the player to connect to this 
     * {@link SinglePlayerGame}.
     * @since 1.0
     */
    @Override
    public void registerPlayer(Socket socket)
    {        
        this.socket = socket;
        
        try {
            output = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (IOException ioEx) {
            gameError("Could not get socket streams. Reason:%n\t%s", 
                    ioEx.getMessage());
            return;
        }
        
        try {            
            output.writeObject(MessageType.JOIN_ACKNOWLEDGE);
            output.writeInt(1);
            output.writeInt(gameID);
            output.flush();
            gameMessage("Registered player.");
        } catch (IOException ioEx) {
            gameError("Failed to register player. Reason:%n\t%s", 
                    ioEx.getMessage());
        }
    }
    
    /**
     * Transfers a card from the {@link Deck}, if available, to the current 
     * {@link IPlayer}.
     * 
     * @param playerID The ID of the player to send the loss message to.
     * @throws IOException Thrown if the {@link Card} cannot be sent to the 
     * {@link IPlayer}.
     * @since 1.0
     */
    @Override
    public void dealCard(int playerID) throws IOException
    {
        try {
            Card c = deck.pullCard();
            output.writeObject(MessageType.CARD_TRANSFER);
            output.writeObject(c);
            output.flush();
        } catch (DeckException deckEx) {
            logger.log(Level.FINE, "Deck emptied!");
        }
    }

    /**
     * Checks the {@link Hand} of the playing {@link IPlayer}. Based on the 
     * outcome of comparing this hand against the dealers, this may send a win 
     * or a loss message to the client.
     * 
     * @param playerID The ID of the player to send the loss message to.
     * @param h The {@link Hand} to compare against the dealer.
     * @throws IOException Thrown if there are any issues sending the win or 
     * loss message to the client.
     * @since 1.0
     */
    @Override
    public void checkHand(int playerID, Hand h) 
            throws IOException
    {
        int plyTotal = h.total();        
        boolean plyHas21 = (plyTotal == 21);
        boolean plyHas5Card = (plyHas21 && h.size() == 5);
        boolean plyHas2Card = (plyHas21 && h.size() == 2);
        
        boolean dealerBust = !dealerPlay(plyTotal);
        int dlrTotal = dealer.total();
        
        gameMessage("Player hand:%n%s", h);
        gameMessage("Dealer hand:%n%s", dealer);
        
        boolean dlrHas21 = (dlrTotal == 21);
        boolean dlrHas5Card = (dlrHas21 && dealer.size() == 5);
        boolean dlrHas2Card = (dlrHas21 && dealer.size() == 2);
        
        if (plyHas2Card) {
            // Player has a Pontoon.
            if (dlrHas2Card) {
                // Player and deal have the same hand, so a push occurs.
                gameMessage("Player and Dealer have a Pontoon! Push.");
                playerWin(playerID, false);
            } else {
                gameMessage("Player wins with a Pontoon!");
                playerWin(playerID, true);
            }
        } else if (plyHas5Card) {
            // Player has a 5-card trick.
            if (dlrHas2Card) {
                // Dealer Pontoon has a higher precedence.
                gameMessage("Dealer wins with a 5-card trick!");
                dealerWin(playerID);
            } else if (dlrHas5Card) {
                // Player and deal have the same hand, so a push occurs.
                gameMessage("Player and Dealer have a 5-card trick! Push.");
                playerWin(playerID, false);
            }else {
                gameMessage("Player wins with a 5-card trick!");
                playerWin(playerID, false);
            }
        } else {
            // Check to see if either the player or dealer is bust.
            if (plyTotal > 21) {
                gameMessage("Player has bust with a score of %d!", plyTotal);
                dealerWin(playerID);
            } else if (dealerBust) {
                gameMessage("Dealer has bust with a score of %d!", dlrTotal);
                playerWin(playerID, plyHas2Card);
            }
            // Any other possible hands.
            else if (plyTotal >= dlrTotal) {
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
    
    /**
     * Lets the dealer take their turn. The basic algorithm will play until the 
     * dealer has a score of up to 21, or until the dealer goes bust. An 
     * alternative algorithm has the dealer comparing against the players score.
     * 
     * @param plyScore The score of the current {@link IPlayer} to compare 
     * against.
     * @return Returns true if the dealer has a score lower than 21, false 
     * otherwise.
     * @since 1.0
     */
    @Override
    public boolean dealerPlay(int plyScore)
    {
        while (dealer.total() <= plyScore) {
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
                logger.log(Level.FINE, "Deck emptied.");
                break;
            }
        }
        return (dealer.total() <= 21);
    }
    
    /**
     * Sends a message to the specified {@link IPlayer} telling them that they 
     * won this hand. If the player won with a Pontoon (2 cards worth 21 points)
     *  , the message can tell them about this.
     * 
     * @param playerID The ID of the player to send the loss message to.
     * @param twentyOne Set to true if the player won with a Pontoon (2 cards 
     * worth 21 points), false otherwise.
     * @throws IOException Thrown if there are any issues sending the message.
     * @since 1.1
     */
    @Override
    public void playerWin(int playerID, boolean twentyOne) throws IOException
    {
        if (twentyOne)
            Server.getInstance().adjustBank(-(bet/2));
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(PLAYER_WIN);
        output.writeObject(dealer);
        output.writeBoolean(twentyOne);
        output.flush();
    }
    
    /**
     * Sends a message to the specified {@link IPlayer} telling them that the 
     * dealer has won this hand.
     * 
     * @param playerID The ID of the player to send the loss message to.
     * @throws IOException Thrown if there are any issues sending the message.
     * @since 1.1
     */
    @Override
    public void dealerWin(int playerID) throws IOException
    {
        Server.getInstance().adjustBank(bet);
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(DEALER_WIN);
        output.writeObject(dealer);
        output.flush();
    }
    
    /**
     * Shuts down this {@link SinglePlayerGame}, closing all the streams and 
     * the socket.
     * 
     * @since 1.0
     */
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
        // Remove this game from the server.
        Server.getInstance().removeGame(gameID);
    }
    
    /**
     * Listens for messages from the {@link IPlayer} taking part in this game in
     *  the background and responds to them as needed.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        if (socket == null || socket.isClosed()) {
            gameMessage("No player registered for this game.");
            stop();
            return;
        }
        
        MessageType reply; // The message sent from the connected client.
        Hand h;
        
        try {
            while (!socket.isClosed()) {
                /* Open the input only if it's not currently open. */
                if (input == null) {
                    try {
                        input = new ObjectInputStream(socket.getInputStream());
                    } catch (IOException ex) { 
                        gameError(ex.getMessage()); 
                    }
                }
                
                try {
                    reply = (MessageType)input.readObject();
                } catch (IOException noMsg) {
                    gameError("Couldn't read message from client. Reason:%n%s", 
                            noMsg.getCause()); 
                    stop();
                    return;
                }
                
                switch (reply) {
                    case CLIENT_READY:
                        this.bet = input.readInt();
                        gameMessage(Level.FINER, "Player set bet to %d", bet);
                        // Initialise the game for a connecting client.
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
                        // Tell a waiting player that they can take their turn.
                        output.writeObject(MessageType.TURN_NOTIFY);
                        output.flush();
                        break;
                    case TURN_RESPONSE:
                        // Respond to a player taking a turn.
                        PlayerAction action = (PlayerAction)input.readObject();
                        switch (action) {
                            case PLAYER_STICK:
                                gameMessage(Level.FINE, "Player has stuck.");
                                h = (Hand)input.readObject();
                                checkHand(1, h);
                                break;
                            case PLAYER_TWIST:
                                gameMessage(Level.FINE, "Player has twisted.");
                                dealCard(1);
                                break;
                            case PLAYER_BUST:
                                h = (Hand)input.readObject();
                                checkHand(1, h);
                                break;
                            default:
                                gameError("Unknown action recieved: '%s'", 
                                        reply);    
                        }
                        break;
                    case CLIENT_DISCONNECT:
                        // Disconnect a client and close the game safely.
                        gameMessage("Player leaving.");
                        stop();
                        break;
                    default:
                        gameError("Unknown message sent to game:%n\t%s", reply);
                }
            }
        } catch (IOException | ClassNotFoundException ioEx) {
            gameError("Error handling single player game. Reason:%n%s", 
                    ioEx.getMessage());
        }
    }

    /**
     * Gets some details about this {@link SinglePlayerGame} and returns them in 
     *  a {@link String}.
     * 
     * @return A String containing details about this {@link SinglePlayerGame}.
     * @since 1.1
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        return String.format("Single-player game - %d (Connected: %s)", 
            gameID, (this.socket != null ? "YES" : "NO"));
    }
}
