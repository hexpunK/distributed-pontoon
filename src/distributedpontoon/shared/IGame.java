/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedpontoon.shared;

/**
 *
 * @author Jordan
 */
public abstract class IGame implements Runnable {
    
    protected static volatile int GameCount;
    protected int gameID;
    
    public void gameMessage(String msg, Object...args)
    {
        msg = String.format(msg, args);
        System.out.printf("GAME %d: %s\n", gameID, msg);
    }
    
    public void gameError(String msg, Object...args)
    {
        msg = String.format(msg, args);
        System.err.printf("GAME %d: %s\n", gameID, msg);
    }

    @Override
    public abstract void run();
}
