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
    /** The {@link ClientGUI} this {@link GUIPlayer} will use to play. */
    private ClientGUI gui;
    
    /**
     * Creates a new {@link GUIPlayer} with a default balance of 500 and a 
     * default bet of 50.
     * 
     * @since 1.0
     */
    public GUIPlayer()
    {
        super();
        this.balance = 500;
        this.bet = 50;
    }
    
    /**
     * Sets up this {@link GUIPlayer}, creating the {@link ClientGUI} and 
     * running any background threads.
     * 
     * @since 1.0
     */
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

    /**
     * Check to see if this {@link GUIPlayer} is in a game or not, and if so, 
     * start running the game.
     * 
     * @since 1.0
     */
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
    }
    
    /**
     * Request a move from this {@link GUIPlayer}. This method will block until 
     * the {@link ClientGUI} tells the server what move to make.
     * 
     * @param caller The {@link IClientGame} that requested the move.
     * @since 1.0
     */
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

    /**
     * Display a message telling this {@link GUIPlayer} that they won the hand. 
     * If they won with a Pontoon (2 cards worth 21 points), the message will 
     * also tell them how many credits they won.
     * 
     * @param game The {@link IClientGame} the player has won.
     * @param pontoon Set to true if this player won with a Pontoon, false 
     * otherwise.
     * @since 1.0
     */
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
            msg = String.format("You won the hand with a Pontoon!%n"
                        + "Adding %d credits to balance.", 
                        game.getBet());
        } else {
            msg = String.format("You won the hand!%n"
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

    /**
     * Display a message telling this {@link GUIPlayer} that they have lost the 
     * hand.
     * 
     * @param game The {@link IClientGame} this player lost.
     * @since 1.0
     */
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

    /**
     * Disconnects the game if it is connected, and resets the {@link ClientGUI}
     *  to the menu state.
     * 
     * @param game The {@link IClientGame} that this player is leaving.
     * @since 1.0
     */
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
