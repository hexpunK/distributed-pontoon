package distributedpontoon.server;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.Card;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link IServerGame} that can handle multiple players 
 * taking part in a game at the same time.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-19
 */
public class MultiPlayerGame extends IServerGame
{
    /** A timeout value to prevent players from doing nothing for too long. */
    public static final int PLAYER_TIMEOUT = 20000;
    /** A mapping of player IDs to their connecting {@link Socket}s. */
    private final ConcurrentHashMap<Integer, Socket> sockets;
    /** A mapping if player IDs to their {@link Hand}s. */
    private final ConcurrentHashMap<Integer, Hand> hands;
    /** A mapping of player IDs to their ready state for playing the game. */
    private final ConcurrentHashMap<Integer, Boolean> playerReady;
    /** A mapping of player IDs to their unique {@link ObjectOutputStream}s. */
    private final ConcurrentHashMap<Integer, ObjectOutputStream> outputs;
    /** A mapping of player IDs to their unique {@link ObjectInputStream}s. */
    private final ConcurrentHashMap<Integer, ObjectInputStream> inputs;
    /** The current number of players in this game. */
    private int playerCount;
    
    /**
     * Sets up a new {@link MultiPlayerGame}.
     * 
     * @since 1.0
     */
    public MultiPlayerGame()
    {
        super();
        this.sockets = new ConcurrentHashMap<>();
        this.hands = new ConcurrentHashMap<>();
        this.playerReady = new ConcurrentHashMap<>();
        this.outputs = new ConcurrentHashMap<>();
        this.inputs = new ConcurrentHashMap<>();
        
        Server.getInstance().registerGame(gameID);
    }
    
    /**
     * Registers a new {@link IPlayer} to this {@link MultiPlayerGame}. If the 
     * game has already started the player will not be registered.
     * 
     * @param socket The {@link Socket} the new {@link IPlayer} connects to 
     * this game with.
     * @since 1.0
     */
    @Override
    public void registerPlayer(Socket socket)
    {        
        if (sockets.containsValue(socket)) return;
        if (playerReady.size() > 0 && isAllReady()) {
            gameMessage("Game is already running.");
            //return;
        }
        
        int playerID = ++playerCount;
        sockets.put(playerID, socket);
        playerReady.put(playerID, false);
        
        try {
            outputs.put(
                    playerID, 
                    new ObjectOutputStream(socket.getOutputStream())
            );
            ObjectOutputStream output = outputs.get(playerID);
            output.writeObject(MessageType.JOIN_ACKNOWLEDGE);
            output.writeInt(playerID);
            output.writeInt(gameID);
            output.flush();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    /**
     * Deals a {@link Card} to the specified {@link IPlayer} if any are left in 
     * the {@link Deck} used in this game.
     * 
     * @param playerID The unique ID for the {@link IPlayer} to deal the 
     * {@link Card} to.
     * @throws IOException Thrown if the {@link Card} cannot be sent to the 
     * client.
     * @since 1.0
     */
    @Override
    public void dealCard(int playerID) throws IOException 
    {
        ObjectOutputStream output = outputs.get(playerID);
        try {
            Card c = deck.pullCard();
            output.writeObject(MessageType.CARD_TRANSFER);
            output.writeObject(c);
            output.flush();
        } catch (Deck.DeckException deckEx) {
            gameMessage("Deck emptied!");
        }
    }
    
    /**
     * Called when all players have either stuck or gone bust, and checks their 
     * hands against the dealers.
     * 
     * @throws IOException Thrown if there are any problems sending the win/loss
     *  message to each player.
     * @since 1.2
     */
    public void checkAllHands() throws IOException
    {
        gameMessage("All players stuck or bust.");
        for (int plyID : hands.keySet()) {
            Hand h = hands.get(plyID);
            checkHand(plyID, h);
        }
    }

    /**
     * Checks the {@link Hand} of a specific {@link IPlayer} and compares it to 
     * the dealers {@link Hand}. If the player wins, {@link 
     * IServerGame#playerWin(int, boolean)} will be called, if the dealer wins 
     * {@link IServerGame#dealerWin(int)} is called instead.
     * 
     * @param playerID The unique ID for the {@link IPlayer} to send messages 
     * to when they win or lose.
     * @param h The {@link Hand} of the {@link IPlayer} to check.
     * @throws IOException Thrown if there are any problems sending the win/ 
     * loss message to the client.
     * @since 1.0
     */
    @Override
    public void checkHand(int playerID, Hand h) throws IOException 
    {
        int plyTotal = h.total();
        
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
            // Any other possible hands.
            if (plyTotal >= dlrTotal) {
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
        while (dealer.total() <= 21) {
            int dlrTotal = dealer.total();
            for (Card c : dealer.getCards()) {
                if (c.Rank == Card.CardRank.ACE) {
                    if (!c.isAceHigh() && (dlrTotal + 10) < plyScore) {
                        c.setAceHigh(true);
                    } else if ((dlrTotal - 10) <= 21) {
                        c.setAceHigh(false);
                    }
                }
            }
            
            try {
                dealer.addCard(deck.pullCard());
            } catch (Deck.DeckException ex) {
                gameMessage("Deck emptied.");
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
     * @param playerID The unique ID for the {@link IPlayer} to send the win 
     * message to.
     * @param twentyOne Set to true if the player has won with a Pontoon (2 
     * cards worth 21 points), false otherwise.
     * @throws IOException Thrown if there is a problem sending the win message 
     * to the client.
     * @since 1.0
     */
    @Override
    public void playerWin(int playerID, boolean twentyOne) throws IOException
    {
        ObjectOutputStream output = outputs.get(playerID);
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(PLAYER_WIN);
        output.writeObject(dealer);
        output.writeBoolean(twentyOne);
        output.flush();
    }
    
    /**
     * Sends a message to the specified {@link IPlayer} telling them that they 
     * lost this hand.
     * 
     * @param playerID The unique ID for the {@link IPlayer} to send the loss 
     * message to.
     * @throws IOException Thrown if there are any problems sending the loss 
     * message to the client.
     * @since 1.0
     */
    @Override
    public void dealerWin(int playerID) throws IOException
    {
        ObjectOutputStream output = outputs.get(playerID);
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(DEALER_WIN);
        output.writeObject(dealer);
        output.flush();
    }
    
    /**
     * Stops this {@link MultiPlayerGame} and removes any {@link IPlayer}s that 
     * haven't disconnected from the game already. Once completed, the game will
     *  unregister itself.
     * 
     * @since 1.0
     */
    @Override
    public void stop()
    {
        for (int playerID : sockets.keySet())
            removePlayer(playerID);
        Server.getInstance().unregisterGame(gameID);
    }
    
    /**
     * Checks to see if all connected {@link IPlayer}s are ready for the next 
     * stage of a game.
     * 
     * @return Returns true if and only if all connected players are ready, 
     * false otherwise.
     * @since 1.1
     */
    private boolean isAllReady()
    {
        for (Boolean status : playerReady.values()) {
            if (!status) return false;
        }
        return true;
    }
    
    /**
     * Removes the specified player from this {@link MultiPlayerGame}. Closes 
     * the streams and socket for the player before removing all their details.
     * 
     * @param playerID The unique ID for the {@link IPlayer} to be removed.
     * @since 1.1
     */
    private void removePlayer(int playerID)
    {
        Socket socket = sockets.get(playerID);
        ObjectInputStream in = inputs.get(playerID);
        ObjectOutputStream out = outputs.get(playerID);
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            socket.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } finally {
            sockets.remove(playerID);
            hands.remove(playerID);
            playerReady.remove(playerID);
            inputs.remove(playerID);
            outputs.remove(playerID);
            playerCount--;
        }
    }
    
    /**
     * Runs in the background waiting for each player to say they are ready and 
     * for their moves when it is their turn.
     * 
     * @since 1.0
     */
    @Override
    public void run()
    {
        HashMap<Integer, Integer> connectTries = new HashMap<>();
        ObjectOutputStream out;
        ObjectInputStream in;
        MessageType reply;
        
        while (playerReady.isEmpty() || !isAllReady()) {
            for (int plyID : sockets.keySet()) {
                if (!playerReady.containsKey(plyID)
                        || playerReady.get(plyID)) continue;
                if (connectTries.containsKey(plyID) 
                        && connectTries.get(plyID) > 5)
                    removePlayer(plyID);
                
                Socket sckt = sockets.get(plyID);
                try {
                    sckt.setSoTimeout(PLAYER_TIMEOUT);
                    // See if the current client is ready or not.
                    if (!inputs.containsKey(plyID)) {
                        inputs.put(
                                plyID, 
                                new ObjectInputStream(sckt.getInputStream())
                        );
                    }
                    in = inputs.get(plyID);
                    reply = (MessageType)in.readObject();
                    if (reply == MessageType.CLIENT_READY) {
                        playerReady.put(plyID, true);
                        // Initialise the game for a connecting client.
                        out = outputs.get(plyID);
                        out.writeObject(MessageType.GAME_INITIALISE);
                        try {
                            out.writeObject(deck.pullCard());
                            out.writeObject(deck.pullCard());
                        } catch (Deck.DeckException deckEx) {
                            gameError(deckEx.getMessage());
                        }
                        out.flush();
                    } else {
                        gameError("Unexpected message from player %d: %s", 
                                plyID, reply);
                    }
                } catch (SocketException sockEx) {
                    gameError("Couldn't set timeout on player %d.", plyID);
                } catch (SocketTimeoutException timeEx) {
                    gameError("Player %d timed out!", plyID);
                    removePlayer(plyID);
                } catch (IOException | ClassNotFoundException ioEx) {
                    gameError("Error communicating with client %d.\n%s", 
                            plyID, ioEx.getMessage());
                }
                if (connectTries.containsKey(plyID))
                    connectTries.put(plyID, connectTries.get(plyID)+1);
                else 
                    connectTries.put(plyID, 1);
            }
        }
        
        try {
            dealer.addCard(deck.pullCard());
            dealer.addCard(deck.pullCard());
        } catch (Deck.DeckException deckEx) {
            gameError(deckEx.getMessage());
        }
        
        for (int plyID : playerReady.keySet())
            playerReady.put(plyID, false);
        
        while (!sockets.isEmpty()) {
        for (int plyID : sockets.keySet()) {
            if (playerReady.get(plyID)) continue;
            Socket sckt = sockets.get(plyID);
            try {
                if (!sckt.isClosed()) {          
                    sckt.setSoTimeout(PLAYER_TIMEOUT);
                    try {
                        in = inputs.get(plyID);
                        reply = (MessageType)in.readObject();
                    } catch (IOException noMsg) {
                        gameError("Error retrieving message. Reason:\n%s", 
                            noMsg.getMessage());
                        stop();
                        return;
                    }

                    out = outputs.get(plyID);
                    switch (reply) {
                        case PLAYER_READY:
                            // Tell a waiting player they can take their turn.
                            out.writeObject(MessageType.TURN_NOTIFY);
                            out.flush();
                            break;
                        case TURN_RESPONSE:
                            // Respond to a player taking a turn.
                            PlayerAction action = (PlayerAction)in.readObject();
                            switch (action) {
                                case PLAYER_STICK:
                                    gameMessage("Player has stuck.");
                                    Hand h = (Hand)in.readObject();
                                    hands.put(plyID, h);
                                    playerReady.put(plyID, true);
                                    if (isAllReady()) checkAllHands();
                                    break;
                                case PLAYER_TWIST:
                                    gameMessage("Player has twisted.");
                                    dealCard(plyID);
                                    break;
                                case PLAYER_BUST:
                                    gameMessage("Player has bust.");
                                    playerReady.put(plyID, true);
                                    dealerWin(plyID);
                                    break;
                                default:
                                    gameError("Unknown action recieved: '%s'", 
                                            reply);    
                            }
                            break;
                        case CLIENT_DISCONNECT:
                            // Disconnect a client and close the game safely.
                            gameMessage("Player %d leaving.", plyID);
                            removePlayer(plyID);
                            break;
                        default:
                            gameError("Unknown message sent to game:\n\t%s", 
                                    reply);
                    }
                }
            } catch (SocketTimeoutException timeEx) {
                gameError("Player %d timed out.", plyID);
                removePlayer(plyID);
            } catch (IOException | ClassNotFoundException ioEx) {
                gameError("Error handling multi-player game. Reason:\n%s", 
                        ioEx.getMessage());
            }
        }
        }
        
        stop();
    }

    /**
     * Gets some details about this {@link MultiPlayerGame} and returns them in 
     * a {@link String}.
     * 
     * @return A String containing details about this {@link MultiPlayerGame}.
     * @since 1.2
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        return String.format("Multi-player game - %d (Players: %d)", 
            gameID, playerCount);
    }
}
