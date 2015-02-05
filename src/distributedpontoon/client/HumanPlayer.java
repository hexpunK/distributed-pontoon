package distributedpontoon.client;

/**
 *
 * @author 6266215
 */
public class HumanPlayer implements IPlayer
{
    private int balance;
    private Game game;
    private Thread gameThread;
    
    @Override
    public void reigsterGame() 
    {
        game = new Game(this, 50);
    }

    @Override
    public void startGame()
    {
        gameThread = new Thread(game);
        gameThread.start();
    }

    @Override
    public void play(Game caller) 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setBalance(int bal) { balance = bal; }

    @Override
    public boolean adjustBalance(int deltaBal)
    { 
        balance += deltaBal;
        return balance > 0;
    }

    @Override
    public int getBalance() { return balance; }
    
    public void viewHand()
    {
        System.out.println("Your hand:");
        System.out.println(game.getHand());
    }
}