package distributedpontoon.client;

import distributedpontoon.client.gui.ClientGUI;
import distributedpontoon.shared.IClientGame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                gui = ClientGUI.getInstance(ply);
                gui.updateBet();
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
    public void startGame()
    {
        if (game == null) {
            JOptionPane.showMessageDialog(gui, 
                    "Please join a server.", 
                    "Error!", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        super.startGame();
        /* Sleep the thread for a few ms to let some values update. */
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(GUIPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        gui.joinGame();
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
                    gui.setHand(game.getHand(), ClientGUI.PLAYER);
                    gui.readyTurn();
                }
            });
            while (!gui.isTurnTaken()) { }
            Thread.sleep(200); // Let the hand update before updating the GUI.
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
    public void playerWin(final IClientGame game, boolean pontoon)
    {
        String msg;
        try {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    gui.setHand(game.getDealerHand(), ClientGUI.DEALER);
                    gui.setHand(game.getHand(), ClientGUI.PLAYER);
                }
            });
        } catch (InterruptedException | InvocationTargetException ex ) {
            System.err.println(ex.getMessage());
        }
        if (pontoon) {
            msg = String.format("You won the hand with a Pontoon!\n"
                        + "Adding %d credits to balance.", 
                        game.getBet());
        } else {
            msg = String.format("You won the hand!\n"
                    + "Returning bet of %d credits.", 
                        game.getBet());
        }
        JOptionPane.showMessageDialog(
                null, 
                msg,
                "Hand Won",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void dealerWin(final IClientGame game)
    {
        try {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    gui.setHand(game.getDealerHand(), ClientGUI.DEALER);
                    gui.setHand(game.getHand(), ClientGUI.PLAYER);
                }
            });
        } catch (InterruptedException | InvocationTargetException ex ) {
            System.err.println(ex.getMessage());
        }
        JOptionPane.showMessageDialog(
                null, 
                String.format("Dealer won the hand. Removing %d credits.", 
                        game.getBet()),
                "Hand Won",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void leaveGame(IClientGame game)
    {
        if (this.game != null && this.game.isConnected())
            this.game.disconnect();
        if (this.gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
        gui.leaveGame();
        playing = true;
    }
}
