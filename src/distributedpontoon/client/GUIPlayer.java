package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Allows a human to play a game of Pontoon using a graphical interface.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-05
 */
public class GUIPlayer extends HumanPlayer
{   
    private ClientGUI gui;
    
    public GUIPlayer()
    {
        super();
        this.balance = 500;
        this.bet = 50;
    }
    
    @Override
    public void init()
    {
        final GUIPlayer ply = this;
        playing = true;
        game = new ClientGame(this, bet, "localhost", 50000);
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                gui = new ClientGUI(ply);
                gui.setGame(game);
                gui.addComponentListener(new ComponentAdapter()
                {
                    @Override
                    public void componentHidden(ComponentEvent ce)
                    {
                        leaveGame(game);
                        ((JFrame)(ce.getComponent())).dispose();
                        playing = false;
                    }
                });
            }
        });
    }
    
    @Override
    public void play(IClientGame caller)
    {
        try {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    gui.setHand(game.getHand());
                    gui.readyTurn();
                }
            });
            while (!gui.isTurnTaken()) { }
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    gui.endTurn();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex ) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void leaveGame(IClientGame game)
    {
        gui.leaveGame();
        if (this.game != null)
            this.game.disconnect();
        if (this.gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        playing = true;
    }
}
