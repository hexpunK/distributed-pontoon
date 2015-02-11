package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.NetMessage.MessageType;
import distributedpontoon.shared.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

/**
 * Allows a human to play a game of Pontoon through the command line interface 
 * being used.
 * 
 * @author 6266215
 * @version 1.3
 * @since 2015-02-08
 */
public class CLIPlayer extends HumanPlayer 
{
    /** Reads the standard input to get the players moves. */
    private final Scanner input;
    
    /**
     * Creates a new {@link CLIPlayer} and sets the input reader up. This player
     *  has an initial balance of 500 credits.
     * 
     * @since 1.0
     */
    public CLIPlayer()
    {
        super();
        this.balance = 500;
        this.bet = 50;
        this.input = new Scanner(System.in);
    }
    
    /**
     * Requests instructions from the user to allow this {@link CLIPlayer} to 
     * connect to new servers, start games, or exit entirely.
     * 
     * @since 1.2
     */
    @Override
    public void init()
    {
        playing = true;
        String line = "";
        String svr = "";
        int port = -1;
        
        while (playing) {
            System.out.println("What would you like to do?");
            line = input.nextLine().trim();
            switch(line) {
                case "servers":
                    // Let the user pick from a list of known servers.
                    Set<Pair<String, Integer>> svrSet = findServers();
                    Pair<String, Integer>[] servers = svrSet.toArray(new Pair[svrSet.size()]);
                    if (servers.length == 0) {
                        System.out.println("No servers found.");
                        return;
                    }
                    System.out.println("Servers available:");
                    int i;
                    for (i = 0; i < servers.length; i++) {
                        System.out.printf(
                                "\t%d - %s:%d\n", 
                                i+1, servers[i].Left, servers[i].Right
                        );
                    }
                    System.out.println("Enter server number: ");
                    String r = input.nextLine();
                    // Attempt to convert the input into a number.
                    int serverNum;
                    try {
                        serverNum = Integer.parseInt(r);
                    } catch (NumberFormatException nfEx) {
                        System.out.println("You can only use numbers!");
                        continue;
                    }
                    if (serverNum <= 0 || serverNum > i) {
                        System.out.printf("You can only enter 1-%d.\n", i);
                        continue;
                    }
                    serverNum--; // Change the number to an index.
                    // Select the server at the specified index.
                    svr = servers[serverNum].Left;
                    port = servers[serverNum].Right;
                    break;
                case "s":
                case "server":
                    System.out.println("Enter new server address/name:");
                    svr = input.nextLine().trim();
                    break;
                case "port":
                    System.out.println("Enter new server port:");
                    port = input.nextInt();
                    input.nextLine();
                    break;
                case "p":
                case "play":
                    if (svr.isEmpty() || port == -1) {
                        System.out.println("Please select a server!");
                        continue;
                    }
                    game = new ClientGame(this, bet, svr, port);
                    startGame();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
                    }
                    break;
                    case "b":
                case "bet":
                    System.out.printf("Current bet is: %d\n", bet);
                    System.out.print("Please enter new bet: ");
                    try {
                        int newBet = input.nextInt();
                        input.nextLine();
                        this.bet = newBet;
                        System.out.println("Bet changed!");
                    } catch (InputMismatchException inEx) {
                        System.err.println("Can only be a number. Bet unchanged.");
                    }
                    break;
                case "bal":
                case "balance":
                    System.out.printf("Current balance: %d\n", getBalance());
                    break;
                case "q":
                case "quit":
                    playing = false;
                    break;
                case "h":
                case "help":
                default:
                    System.out.println(helpMenu());
            }
        }
    }
    
    /**
     * Queries the users move in response to a {@link ClientGame} receiving a {@link 
     * MessageType#TURN_NOTIFY} message from a server. Moves that do not sent a 
     * message back to the server will allow the user to input another once 
     * complete.
     * 
     * @param caller The {@link IClientGame} instance that has asked for the 
     * players move.
     * @since 1.0
     */
    @Override
    public void play(IClientGame caller)
    {
        System.out.println("Please enter your move...");
        String move = input.nextLine().trim();
        
        switch (move) {
            case "s":
            case "stick":
                caller.stand();
                break;
            case "t":
            case "twist":
                caller.twist();
                break;
            case "bal":
            case "balance":
                System.out.printf("Current balance: %d\n", getBalance());
                break;
            case "h":
            case "hand":
                viewHand();
                play(caller);
                break;
            case "a":
            case "aces":
                setAces();
                play(caller);
                break;
            case "q":
            case "quit":
                leaveGame(caller);
                break;
            case "help":
            default:
                System.out.println(helpMessage());
                play(caller);
        }
    }

    @Override
    public void playerWin(IClientGame game, boolean pontoon) 
    {
        if (pontoon) {
            System.out.printf("You won with a pontoon! Adding %d credits.\n", 
                    game.getBet());
        } else {
            System.out.printf("You won! Bet of %d returned.\n", game.getBet());
        }
        System.out.printf("Current balance: %d\n", balance);
    }

    @Override
    public void dealerWin(IClientGame game)
    { 
        System.out.printf("Dealer won. Removing %d credits.\n", game.getBet());
        System.out.printf("Current balance: %d\n", balance);
    }

    /**
     * Prints out the contents and total point value of the current {@link Hand}
     *  for this {@link CLIPlayer} to the standard output.
     * 
     *  @since 1.0
     */
    public void viewHand()
    {
        System.out.println("Your hand:");
        System.out.println(game.getHand());
    }
    
    /**
     * Allows the player to change the high/low status of any {@link 
     * CardRank#ACE} {@link Card}s in their hand. When selecting a card to swap 
     * the user must not enter a value below 1 or higher than the number of 
     * aces held, doing so will return from the method with no changes.
     * 
     * @since 1.3
     */
    public void setAces()
    {
        ArrayList<Card> cards = game.getHand().getCards();
        ArrayList<Card> aces = new ArrayList<>();
        int aceCount = 0;
        /* Work out which cards in the hand are aces and which aren't. */
        for (Card c : cards) {
            if (c.Rank == Card.CardRank.ACE) {
                aces.add(c);
                System.out.printf("(%d) %s,\n", ++aceCount, c);
            }
        }
        if (aces.size() > 0) {
            /* Let the user change the value of aces. */
            System.out.printf("You have %d aces.\n", aceCount);
            System.out.println("Select an ace to switch ('no' to stop):");
            String r = input.nextLine();
            switch (r) {
                case "no":
                    // Don't change anything.
                    return;
                default:
                    // Attempt to convert the input into a number and find an 
                    // ace at that index.
                    int aceNum;
                    try {
                        aceNum = Integer.parseInt(r);
                    } catch (NumberFormatException nfEx) {
                        System.out.println("You can only use numbers!");
                        return;
                    }
                    if (aceNum <= 0 || aceNum > aceCount) {
                        System.out.printf("You can only enter 1-%d.\n", 
                                aceCount);
                        return;
                    }
                    aceNum--;
                    /* Toggle the state of an Ace. */
                    aces.get(aceNum).setAceHigh(!aces.get(aceNum).isAceHigh());
            }
        } else {
            System.out.println("You have no aces.");
        }
    }
    
    /**
     * Creates a help message to be displayed when this {@link CLIPlayer} asks 
     * for help during a game.
     * 
     * @return The help message as a String.
     * @since 1.1
     */
    private String helpMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Pontoon Client CLI - Game Help:\n");
        sb.append("\tCommand (Short) - Action\n");
        sb.append("\tstick (s) - Tells the dealer you don't want any more cards"
                + " this round.\n");
        sb.append("\ttwist (t) - Requests another card from the dealer.\n");
        sb.append("\taces (a) - Lets you change the value of any ace in your "
                + "hand.\n");
        sb.append("\tbalance (bal) - Displays your current balance.\n");
        sb.append("\thand (h) - Displays the cards in your hand and their total"
                + " point value.\n");
        sb.append("\tquit (q) - Exits the current game.");
        
        return sb.toString();
    }
    
    /**
     * Creates a help menu for the {@link CLIPlayer} main menu.
     * 
     * @return A String containing the help message for a {@link CLIPlayer}.
     * @since 1.2
     */
    private String helpMenu()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Pontoon Client CLI - Menu Help:\n");
        sb.append("\tCommand (Short) - Action\n");
        sb.append("\tserver (s) - Lets you select a new server to play on.\n");
        sb.append("\tport - Lets you select a new port to connect with.\n");
        sb.append("\tplay (p) - Starts a game with the current server.\n");
        sb.append("\tbet (b) - Adjusts the bet for the next hand by the "
                + "specified amount.\n");
        sb.append("\tbalance (bal) - Displays your current balance.\n");
        sb.append("\thelp (h) - Displays this help message.\n");
        sb.append("\tquit (q) - Exits the CLI client.\n");
        
        return sb.toString();
    }
}
