package distributedpontoon.client;

import distributedpontoon.shared.PontoonLogger;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Initialises various clients for the distributed Pontoon application. Supports
 *  the {@link CLIPlayer}, {@link GUIPlayer} and {@link RoboPlayer} clients.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
 */
public class Client
{   
    public static void main(String[] args)
    {        
        try {
            PontoonLogger.setup("client");
        } catch (IOException ex) {
            System.err.printf("Error setting up logging. Reason\n%s", 
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
                    player = new RoboPlayer();
                    player.init();
                    break;
                case "exit":
                case "quit":
                    return;
                default:
                    System.err.printf("Unknown client type '%s'!\n", run);
            }
            if (player != null)
                while (player.isPlaying()) {}
        }
        
        try {
            PontoonLogger.close();
        } catch (IOException ex) {
            System.err.printf("Failed to close logger. Reason:\n%s", 
                    ex.getMessage());
        }
    }
}
