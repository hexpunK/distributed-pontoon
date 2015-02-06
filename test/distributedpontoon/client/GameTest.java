package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.stubs.StubbedPlayer;
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
    
    private static StubbedServer server;
    private Game instance;
    private Thread gameThread;
    private StubbedPlayer player;
    
    @BeforeClass
    public static void beforeClass() {
        System.out.println("Setting up GameTest...");
        server = new StubbedServer();
        server.init();
        System.out.println("\tStubbedServer running.");
    }
    
    @Before
    public void beforeTest() {
        player = new StubbedPlayer();
        instance = new Game(player, 50, "localhost", 50000);
        gameThread = new Thread(instance);
        gameThread.start();
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);
    }
    
    @After
    public void afterTest() throws InterruptedException {
        instance.disconnect();
        gameThread.join();
    }
    
    @AfterClass
    public static void afterClass() throws InterruptedException {
        System.out.println("Tearing down GameTest...");
        server.kill();
        System.out.println("\tServer terminated.");
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
        assertEquals(1, expResult.size());
        System.out.println("\tSuccess! Adding a card changed the hand.");
        assertEquals(0, instance.getHand().size());
        System.out.println("\tSuccess! Players hand did not change.");
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
        Hand hOld = instance.getHand();
        assertEquals(0, hOld.size());
        instance.twist();
        Hand hNew = instance.getHand();
        assertEquals(1, hNew.size());
        System.out.println("\tSuccess! Twisting added a card.");
    }

    /**
     * Test of stand method, of class Game.
     */
    @Test
    public void testStand() {
        System.out.println("Testing: stand");
        Hand hOld = instance.getHand();
        int oldBal = player.getBalance();
        assertEquals(0, hOld.size());
        assertEquals(500, oldBal);
        instance.stand();
        Hand hNew = instance.getHand();
        int newBal = player.getBalance();
        assertEquals(0, hNew.size());
        System.out.println("\tSuccess! Standing did not change the hand.");
        assertEquals(500, newBal);
        assertEquals(oldBal, newBal);
        System.out.println("\tSuccess! Standing did not change the balance.");
    }

    /**
     * Test of bust method, of class Game.
     */
    @Test
    public void testBust() {
        System.out.println("Testing: bust");
        int oldBal = player.getBalance();
        assertTrue("Old balance isn't 500.", 500 == oldBal);
        instance.bust();
        int newBal = player.getBalance();
        assertFalse("New balance is still 500.", 500 == newBal);
        assertTrue("New balance is not lower than old.", oldBal > newBal);
        System.out.println("\tSuccess! Busting did change the balance.");
    }
}
