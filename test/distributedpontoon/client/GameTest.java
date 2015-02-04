/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.stubs.StubbedServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author 6266215
 */
public class GameTest {
    
    static StubbedServer server;
    static Thread serverThread;
    static Game instance;
    
    @BeforeClass
    public static void beforeTest() {
        System.out.println("Setting up GameTest...");
        server = new StubbedServer();
        serverThread = new Thread(server);
        serverThread.start();
        
        instance = new Game(null, "localhost", 50000);
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);
        System.out.println("\tConnected to the stubbed server.");
    }
    
    @AfterClass
    public static void afterTest() throws InterruptedException {
        System.out.println("Tearing down GameTest...");
        instance.disconnect();
        System.out.println("\tGame disconnected from stubbed server.");
        server.kill();
        serverThread.join();
    }

    /**
     * Test of getHand method, of class Game.
     */
    @Test
    public void testGetHand() {
        System.out.println("Testing: getHand");
        Hand expResult = new Hand();
        Hand result = instance.getHand();
        assertNotNull(result);
        assertEquals(0, result.size());
        System.out.println("\tSuccess! Fresh hand is empty.");
        expResult.addCard(new Card(Card.CardSuit.CLUBS, Card.CardRank.ACE));
    }

    /**
     * Test of isConnected method, of class Game.
     */
    @Test
    public void testIsConnected() {
        System.out.println("Testing: isConnected");
        boolean expResult = true;
        boolean result = instance.isConnected();
        assertEquals(expResult, result);
        System.out.println("\tGame is connected to stubbed server.");
    }

    /**
     * Test of twist method, of class Game.
     */
    @Test
    public void testTwist() {
        System.out.println("Testing: twist");
        instance.twist();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stand method, of class Game.
     */
    @Test
    public void testStand() {
        System.out.println("Testing: stand");
        instance.stand();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of bust method, of class Game.
     */
    @Test
    public void testBust() {
        System.out.println("Testing: bust");
        instance.bust();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showHand method, of class Game.
     */
    @Test
    public void testShowHand() {
        System.out.println("Testing: showHand");
        instance.showHand();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }    
}
