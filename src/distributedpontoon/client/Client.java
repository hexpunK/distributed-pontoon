package distributedpontoon.client;

/**
 *
 * @author 6266215
 */
public class Client
{    
    public static void main(String[] args)
    {        
        IPlayer player = new CLIPlayer();
        Game g = new Game(player, 50);
        player.reigsterGame(g);
        player.startGame();
    }
}
