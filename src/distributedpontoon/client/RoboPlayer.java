package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.Triple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * An automated Pontoon player. This player will use the same tactic each game, 
 * calling {@link IClientGame#twist()} as long as the current value of the 
 * {@link Hand} is below a randomised threshold value (1 to 21 inclusive).
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-02-18
 */
public class RoboPlayer extends IPlayer
{
    /** The threshold for twisting for this {@link RoboPlayer}. */
    private final int threshold;
    /** A mapping of {@link IClientGame}s to their executing {@link Thread}s. */
    private final ConcurrentHashMap<IClientGame, Thread> games;
    /** A mapping of {@link IClientGame}s to the player ID for each game. */
    private final ConcurrentHashMap<IClientGame, Integer> playerIDs;
    
    /**
     * Creates a new {@link RoboPlayer} with a randomised threshold value and 
     * no games assigned.
     * 
     * @since 1.0
     */
    public RoboPlayer()
    {
        Random randomiser = new Random();
        this.threshold = randomiser.nextInt(21);
        this.games = new ConcurrentHashMap<>();
        this.playerIDs = new ConcurrentHashMap<>();
        this.balance = 300;
    }
    
    /**
     * Gathers a list of known servers and games from the directory server, then
     *  attempts to connect to each single player capable server.
     * 
     * @since 1.0
     */
    @Override
    public void init()
    {
        System.out.printf("RoboPlayer with threshold %d started.%n", threshold);
        System.out.printf("Joining %d game(s) per server.%n", Client.MAX_GAMES);
        Set<Triple<String, Integer, Integer>> servers = findServers();
        if (servers == null || servers.isEmpty()) return;
        for (Triple server : servers) {
            String address = (String)server.One;
            int tmpPort = (int)server.Two;
            if ((int)server.Three >= 0) continue; // Ignore MP games.
            for (int i = 0; i < Client.MAX_GAMES; i++) {
                IClientGame game = new ClientGame(this, 50, address, tmpPort);
                game.setGameID((int)server.Three);
                Thread t = new Thread(game);
                t.start();
                games.put(game, t);
            }
        }
        playing = true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Sets the player ID for the specified game to the specified int.
     * 
     * @param game The {@link IClientGame} to change the player ID of.
     * @param id The new player ID as an int.
     * @since 1.0
     */
    @Override
    public void setPlayerID(IClientGame game, int id)
    {
        if (game == null) return;
        playerIDs.put(game, id);
    }
    
    /**
     * Adds a new {@link IClientGame} to this {@link RoboPlayer}.
     * 
     * @param game The {@link IClientGame} to take part in.
     * @since 1.0
     */
    @Override
    public void reigsterGame(IClientGame game)
    {
        if (game == null) return;
        Thread gameThread = new Thread(game);
        games.put(game, gameThread);
    }

    /**
     * Checks to see if this {@link RoboPlayer} is still playing a game of 
     * Pontoon.
     * 
     * @return Returns true if this player is still in a game, false otherwise.
     * @since 1.0
     */
    @Override
    public synchronized boolean isPlaying() { return playing; }
    
    /**
     * Starts any stored {@link IClientGame}s. If the {@link RoboPlayer} runs 
     * out of funding, it will stop connecting to games.
     * 
     * @since 1.0
     */
    @Override
    public void startGame()
    {
        ArrayList<IClientGame> toLeave = new ArrayList<>();
        for (IClientGame g : games.keySet()) {
            if (g.isConnected()) {
                if (balance >= g.getBet())
                    g.startGame();
                else {
                    g.gameError("Insufficient funds for game");
                    toLeave.add(g);
                }
            } else {
                g.gameError("Game not connected.");
                toLeave.add(g);
            }
        }
        
        for (IClientGame g : toLeave) {
            leaveGame(g);
        }
    }

    /**
     * The specified {@link IClientGame} calls this when it needs input from 
     * this {@link RoboPlayer}. The {@link RoboPlayer} will always call {@link 
     * IClientGame#twist()} until their hand has a value over their threshold.
     * 
     * @param caller The {@link IClientGame} that needs a move from this player.
     * @since 1.0
     */
    @Override
    public void play(IClientGame caller)
    {
        if (caller == null) return;
        if (caller.getHand().total() < threshold) {
            caller.twist();
            Hand hand = caller.getHand();
            // Attempt to make Aces high if it will help the score.
            for (Card card : hand.getCards()) {
                if (card.Rank == Card.CardRank.ACE
                        && !card.isAceHigh()
                        && (hand.total() + 10) <= 21) {
                    card.setAceHigh(!card.isAceHigh());
                }
            }
        } else {
            caller.stand();
        }
    }
    
    /**
     * Prints the details of this {@link RoboPlayer}s win to standard output.
     * 
     * @param game The {@link IClientGame} this player has won.
     * @param pontoon Set to true if the player won with a pontoon (2 cards 
     * totalling 21 points), false otherwise.
     * @since 1.1
     */
    @Override
    public synchronized void playerWin(IClientGame game, boolean pontoon) 
    {
        if (game == null) return;
        if (pontoon) {
            game.gameMessage("Player won with a pontoon! Added %d credits.", 
                    game.getBet());
        } else {
            game.gameMessage("Player won! Bet of %d returned.",
                    game.getBet());
        }
        System.out.printf("Current balance: %d%n", balance);
    }

    /**
     * Prints the details of this {@link RoboPlayer}s loss to the standard 
     * output.
     * 
     * @param game The {@link IClientGame} this player lost.
     * @since 1.1
     */
    @Override
    public synchronized void dealerWin(IClientGame game)
    { 
        if (game == null) return;
        game.gameMessage("Dealer won. Removed %d credits.", game.getBet());
        System.out.printf("Current balance: %d%n", balance);
    }
    
    /**
     * Disconnects this {@link RoboPlayer} from the specified {@link 
     * IClientGame}.
     * 
     * @param game The {@link IClientGame} to disconnect from.
     */
    @Override
    public synchronized void leaveGame(IClientGame game)
    {
        if (game == null) return;
        game.disconnect();
        try {
            Thread t = games.remove(game);
            t.join(1000);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        
        if (games.isEmpty())
            playing = false;
    }
}
