package distributedpontoon.shared;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Card.CardSuit;

/**
 * Unit tests to ensure the more complex functionality of a {@link Card} object 
 * works as expected. Tests the creation of each unique possibility for a 
 * standard playing card deck, and if the instances of Card will return the 
 * correct values when using Aces high and low.
 * 
 * @author 6266215
 */
public class CardTest {
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Initialising Card tests.");
        assertEquals("Failed to create all cards.", 52, Card.ALL_CARDS.size());
        System.out.println("All 52 Card objects exist!");
    }

    /**
     * Test of setAceHigh method, of class Card.
     */
    @Test
    public void testSetAceHigh() {
        System.out.println("Testing: setAceHigh");
        Card instance = new Card(CardSuit.SPADES, CardRank.ACE);
        assertNotNull("Card could not be created!", instance);
        assertTrue("Low ace didn't return 1!", instance.getValue() == 1);
        instance.setAceHigh(true); // Change to ace high.
        assertTrue("High ace didn't return 11!", instance.getValue() == 11);
        System.out.println("\tSuccess! Aces can be both low and high.");
    }

    /**
     * Test of toString method, of class Card.
     */
    @Test
    public void testToString() {
        System.out.println("Testing: toString");
        Card instance = new Card(CardSuit.SPADES, CardRank.ACE);
        assertNotNull("Card could not be created!", instance);
        String result = instance.toString();
        assertEquals("toString returned an incorrect result.", 
                "ACE OF SPADES (1)", result);
        System.out.printf("\tSuccess! toString output was %s.\n", result);
    }
    
}
