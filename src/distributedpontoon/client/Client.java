package distributedpontoon.client;

import java.util.Scanner;

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
    }
}
