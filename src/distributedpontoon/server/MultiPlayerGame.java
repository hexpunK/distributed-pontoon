package distributedpontoon.server;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
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
 */
public class MultiPlayerGame extends IServerGame
{
    public static final int PLAYER_TIMEOUT = 20000;
    private final ConcurrentHashMap<Integer, Socket> sockets;
    private final ConcurrentHashMap<Integer, Hand> hands;
    private final ConcurrentHashMap<Integer, Boolean> playerReady;
    private final ConcurrentHashMap<Integer, ObjectOutputStream> outputs;
    private final ConcurrentHashMap<Integer, ObjectInputStream> inputs;
    private int playerCount;
    
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
    
    public void checkAllHands() throws IOException
    {
        gameMessage("All players stuck or bust.");
        for (int plyID : hands.keySet()) {
            Hand h = hands.get(plyID);
            checkHand(plyID, h);
        }
    }

    @Override
    public void checkHand(int playerID, Hand h) 
            throws IOException 
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
    
    @Override
    public void dealerWin(int playerID) throws IOException
    {
        ObjectOutputStream output = outputs.get(playerID);
        output.writeObject(MessageType.GAME_RESULT);
        output.writeBoolean(DEALER_WIN);
        output.writeObject(dealer);
        output.flush();
    }
    
    @Override
    public void stop()
    {
        for (int playerID : sockets.keySet())
            removePlayer(playerID);
        Server.getInstance().unregisterGame(gameID);
    }
    
    private boolean isAllReady()
    {
        for (Boolean status : playerReady.values()) {
            if (!status) return false;
        }
        return true;
    }
    
    private void removePlayer(int playerID)
    {
        Socket socket = sockets.get(playerID);
        try {
            socket.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
        sockets.remove(playerID);
        playerReady.remove(playerID);
        playerCount--;
    }
    
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
}
