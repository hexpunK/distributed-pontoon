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
        
        System.out.println("Run which client? (gui, cli, robo)");
        String run = input.nextLine();
        run = run.trim();
        IPlayer player;
        Game g;
        
        switch (run) {
            case "gui":
                System.out.println("Starting GUI client...");
                player = new GUIPlayer();
                g = new Game(player, 50);
                player.reigsterGame(g);
                player.startGame();
                break;
            case "cli":
                System.out.println("Starting CLI client...");
                player = new CLIPlayer();
                g = new Game(player, 50);
                player.reigsterGame(g);
                player.startGame();
                break;
            case "robo":
                System.out.println("Starting automated clients...");
                break;
            default:
                System.err.printf("Unknown client type '%s'!\n", run);
        }
    }
}
