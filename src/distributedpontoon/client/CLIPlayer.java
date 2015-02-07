package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.NetMessage.MessageType;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Allows a human to play a game of Pontoon through the command line interface 
 * being used.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-07
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
        boolean playing = true;
        String line;
        String svr = "localhost";
        int port = 50000;
        
        while (playing) {
            System.out.println("What would you like to do?");
            line = input.nextLine().trim();
            switch(line) {
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
                    game = new ClientGame(this, 50, svr, port);
                    startGame();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
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
            case "b":
            case "bet":
                System.out.printf("Current bet is: %d\n", caller.getBet());
                System.out.print("Please enter new bet: ");
                try {
                    int bet = input.nextInt();
                    input.nextLine();
                    caller.setBet(bet);
                    System.out.println("Bet changed!");
                } catch (InputMismatchException inEx) {
                    System.err.println("You must enter a number. Bet unchanged.");
                }
                play(caller);
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

    /**
     * Prints out the contents and total point value of the current {@link Hand}
     *  for this {@link CLIPlayer} to the standard output.
     * 
     *  @since 1.0
     */
    @Override
    public void viewHand()
    {
        System.out.println("Your hand:");
        System.out.println(game.getHand());
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
        sb.append("\tbet (b) - Adjusts the bet for this hand by the specified "
                + "amount.\n");
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
        sb.append("\tbalance (bal) - Displays your current balance.\n");
        sb.append("\thelp (h) - Displays this help message.\n");
        sb.append("\tquit (q) - Exits the CLI client.\n");
        
        return sb.toString();
    }
}
