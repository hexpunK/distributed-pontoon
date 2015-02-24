package distributedpontoon.client;

import distributedpontoon.shared.PontoonLogger;
import java.io.IOException;
import java.util.Scanner;

/**
 * Initialises various clients for the distributed Pontoon application. Supports
 *  the {@link CLIPlayer}, {@link GUIPlayer} and {@link RoboPlayer} clients.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-02-20
 */
public class Client
{   
    /** The maximum number of games the {@link RoboPlayer} should play against 
     a single server. */
    public static int MAX_GAMES = 10;
    
    public static void main(String[] args)
    {        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--max-games":
                    try {
                        int games = Integer.parseInt(args[++i]);
                        if (games <= 0) {
                            System.err.println("max-games only accepts numbers"
                                    + " greater than zero!");
                            i--;
                            continue;
                        }
                        Client.MAX_GAMES = games;
                    } catch (NumberFormatException ex) {
                        System.err.println("The max-games argument only accepts"
                                + " numbers!");
                        i--;
                    }
                    break;
                case "--no-file":
                    System.out.println("Disabling logging to file...");
                    PontoonLogger.fileLog = false;
                    break;
                case "-v":
                case "--verbose":
                    System.out.println("Enabling verbose output...");
                    PontoonLogger.verbose = true;
                    break;
                case "-h":
                case "--help":
                    System.out.println(helpMessage());
                    return;
                default:
                    System.out.printf("Unknown arugment %s%n", args[i]);
            }
        }
        
        try {
            PontoonLogger.setup("client");
        } catch (IOException ex) {
            System.err.printf("Error setting up logging. Reason%n%s", 
                    ex.getMessage());
        }
        Scanner input = new Scanner(System.in);
        boolean running = true;
        
        IPlayer player = null;
        
        while (running) {
            System.out.println("Run which client? (gui, cli, robo)");
            String run = input.nextLine().trim();
            switch (run) {
                case "gui":
                    // Launch the GUI client.
                    System.out.println("Starting GUI client...");
                    player = new GUIPlayer();
                    player.init();
                    break;
                case "cli":
                    // Launch the CLI client.
                    System.out.println("Starting CLI client...");
                    player = new CLIPlayer();
                    player.init();
                    break;
                case "robo":
                    // Launch a automated client.
                    System.out.println("Starting automated clients...");
                    System.out.println("How many client do you want?");
                    String in = input.nextLine();
                    int num = -1;
                    try {
                        num = Integer.parseInt(in);
                    } catch (NumberFormatException ex) {
                        System.out.println("You must specify a number.");
                    }
                    if (num > 0) {
                        for (int i = 0; i < num; i++) {
                            player = new RoboPlayer();
                            player.init();
                            player.startGame();
                        }
                    } else {
                        System.out.println("Cannot start less than one robo.");
                    }
                    break;
                case "exit":
                case "quit":
                    return;
                default:
                    System.err.printf("Unknown client type '%s'!%n", run);
            }
            if (player != null)
                while (player.isPlaying()) {}
        }
        
        try {
            PontoonLogger.close();
        } catch (IOException ex) {
            System.err.printf("Failed to close logger. Reason:%n%s", 
                    ex.getMessage());
        }
    }
    
    /**
     * Prints a help message to explain the command line options for the client.
     * 
     * @return Returns a String containing the command line help.
     * @since 1.1
     */
    private static String helpMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Distributed Pontoon Client Help:\n");
        sb.append("\tCommand [options] (Short) - Action\n");
        sb.append("\t--max-games [games] - Specifies the maximum number of "
                + "times the roboplayer should play on a server.\n");
        sb.append("\t--no-file - Prevents logging to a file.\n");
        sb.append("\t--verbose (-v) - Prints extra detail to the console.");
        sb.append("\t--help (-h) - Displays this help message.\n");
        
        return sb.toString();
    }
}
