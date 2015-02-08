package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;

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
    }
    
    @Override
    public void init()
    {
        playing = true;
        game = new ClientGame(this, bet, "localhost", 50000);
        gui = new ClientGUI(this);
        gui.setGame(game);
        gui.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent ce)
            {
                leaveGame(game);
                ((JFrame)(ce.getComponent())).dispose();
            }
        });
    }
    
    @Override
    public void play(IClientGame caller)
    {
        gui.readyTurn();
    }

    @Override
    public void leaveGame(IClientGame game)
    {
        gui.leaveGame();
        super.leaveGame(game);
    }
}
