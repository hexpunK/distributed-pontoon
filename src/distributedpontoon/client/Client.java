package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import java.util.Scanner;

/**
 *
 * @author 6266215
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
                    System.out.println("Starting GUI client...");
                    player = new GUIPlayer();
                    player.init();
                    break;
                case "cli":
                    System.out.println("Starting CLI client...");
                    player = new CLIPlayer();
                    player.init();
                    break;
                case "robo":
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
