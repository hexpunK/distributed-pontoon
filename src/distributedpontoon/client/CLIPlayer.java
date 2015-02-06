package distributedpontoon.client;

import distributedpontoon.shared.NetMessage.MessageType;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Allows a human to play a game of Pontoon through the command line interface 
 * being used.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-05
 */
public class CLIPlayer extends HumanPlayer 
{
    /** Reads the standard input to get the players moves. */
    private Scanner input;
    
    /**
     * Creates a new {@link CLIPlayer} and sets the input reader up.
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
     * Queries the users move in response to a {@link Game} receiving a {@link 
     * MessageType#TURN_NOTIFY} message from a server. Moves that do not sent a 
     * message back to the server will allow the user to input another once 
     * complete.
     * 
     * @param caller The {@link Game} instance that has asked for the players 
     * move.
     * @since 1.0
     */
    @Override
    public void play(Game caller)
    {
        System.out.println("Please enter your move...");
        String move = input.nextLine();
        move = move.trim();
        
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
                } catch (IllegalArgumentException argEx) {
                    System.err.println(argEx.getMessage());
                }
                play(caller);
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
                StringBuilder sb = new StringBuilder();
                sb.append("Pontoon CLI Help:\n");
                sb.append("\tCommand (Short)\t-\tAction\n");
                sb.append("\tstick (s)\t-\tTells the dealer you don't want any more cards this round.\n");
                sb.append("\ttwist (t)\t-\tRequests another card from the dealer.\n");
                sb.append("\tbet (b)\t-\tAdjusts the bet for this hand by the specified amount.\n");
                sb.append("\thand (t)\t-\tDisplays the cards in your hand and their total point value.\n");
                sb.append("\tquit (q)\t-\tExits the current game.");
                System.out.println(sb.toString());
                play(caller);
        }
    }

    @Override
    public void viewHand()
    {
        System.out.println("Your hand:");
        System.out.println(game.getHand());
    }
}
