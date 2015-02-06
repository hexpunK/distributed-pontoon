package distributedpontoon.client;

import distributedpontoon.shared.IClientGame;

/**
 * Allows a human to play a game of Pontoon using a graphical interface.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-05
 */
public class GUIPlayer extends HumanPlayer
{
    public GUIPlayer()
    {
        super();
    }
    
    @Override
    public void play(IClientGame caller)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void viewHand()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
