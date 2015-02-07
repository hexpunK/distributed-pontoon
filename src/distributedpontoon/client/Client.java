package distributedpontoon.client;

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
        
        IPlayer player;
        Game g;
        
        while (running) {
            System.out.println("Run which client? (gui, cli, robo)");
            String run = input.nextLine().trim();
            switch (run) {
                case "gui":
                    System.out.println("Starting GUI client...");
                    player = new GUIPlayer();
                    g = new Game(player, 50);
                    player.reigsterGame(g);
                    player.init();
                    break;
                case "cli":
                    System.out.println("Starting CLI client...");
                    player = new CLIPlayer();
                    g = new Game(player, 50);
                    player.reigsterGame(g);
                    player.init();
                    break;
                case "robo":
                    System.out.println("Starting automated clients...");
                    break;
                case "exit":
                case "quit":
                    running = false;
                    break;
                default:
                    System.err.printf("Unknown client type '%s'!\n", run);
            }
        }
    }
}
