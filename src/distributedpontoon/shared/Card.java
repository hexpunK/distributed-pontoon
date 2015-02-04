package distributedpontoon.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a standard playing card. Cards can have values ranging from Ace to
 *  King (represented in {@link CardRank}), and must be one of the suits 
 * (represented in {@link CardSuit}). Once created a Card instance is immutable.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-03
 */
public class Card implements Serializable
{    
    /**
     * An enumeration of the valid values for cards in a standard playing card 
     * deck. Each enumerated value has an integer value corresponding to the 
     * point value of the card.
     * 
     * @version 1.0
     * @since 2015-02-03
     */
    public static enum CardRank
    {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(10),
        QUEEN(10),
        KING(10);
        
        /** The point value of the current card. */
        private final int value;
        
        CardRank(int points)
        { 
            this.value = points;
        }
    }
    
    /**
     * An enumeration of the valid suits for cards in a standard playing card 
     * deck. Each enumerated value provides the name for the suit.
     * 
     * @version 1.0
     * @since 2015-02-03
     */
    public static enum CardSuit
    {
        DIAMONDS,
        HEARTS,
        SPADES,
        CLUBS;
    }
    
    /**
     * A {@link Set} of all possible {@link Card} objects based on the 
     * combinations provided from using the {@link CardRank} and 
     * {@link CardSuit} enumerations.
     * 
     * @since 1.0
     */
    public static final Set<Card> ALL_CARDS;
    
    static {
        // Static initialiser to give us all possible cards.
        ALL_CARDS = new HashSet<>();
        int count = 0;
        for (Card.CardSuit suit : Card.CardSuit.values()) {
            for (Card.CardRank rank : Card.CardRank.values()) {
                ALL_CARDS.add(new Card(suit, rank));
                count++;
            }
        }
        System.out.printf("Created %d cards.\n", count);
        if (count < 52) {
            // Ensure a full set of cards.
            System.err.println("Failed to create all 52 cards!");
            System.exit(-1);
        } 
    }
    
    /** The suit of this card as a {@link CardSuit} value. */
    public final CardSuit Suit;
    /** The point value of the card as a {@link CardRank}. */
    public final CardRank Rank;
    /** If the current card is an Ace, we can make it count as 11 instead. */
    private boolean aceHigh;
    
    /**
     * Creates a new Card with the specified suit and point value.
     * 
     * @param suit The suit of this Card as a {@link CardSuit} value.
     * @param rank The point value of this Card as a {@link CardRank} value.
     * @since 1.0
     */
    public Card(CardSuit suit, CardRank rank)
    {
        this.Suit = suit;
        this.Rank = rank;
    }
    
    /**
     * Sets the 'ace high' flag for this {@link Card} to the provided value. 
     * Having this set to true causes the card to count as 11 rather than 1 
     * when it is an {@link CardRank#ACE}.
     * 
     * @param high Set this to true if this {@link Card} needs to play Aces high
     *  , other {@link CardRank} values are unaffected by this.
     * @since 1.0
     */
    public final void setAceHigh(boolean high) { aceHigh = high; }
    
    /**
     * Checks if this {@link Card} is being played as a high or low ace.
     * 
     * @return Returns true if this {@link Card} is playing Ace as 11 points 
     * rather than 1 point. Returns false otherwise.
     * @since 1.0
     */
    public final boolean isAceHigh() { return aceHigh; }
    
    /**
     * Gets the point value for this {@link Card}. As {@link CardRank#ACE} can 
     * be played high or low, this may return either 1 or 11 for an Ace.
     * 
     * @return Returns the point value for this {@link Card} as an int.
     * @since 1.0
     */
    public int getValue() 
    { 
        if (this.Rank == CardRank.ACE && aceHigh) {
            return 11;
        } else {
            return this.Rank.value;
        }
    }
    
    /**
     * Prints the value and suit of this {@link Card}.
     * 
     * @return The name of this card as a {@link String}.
     * @since 1.0
     */
    @Override
    public String toString()
    {
        return String.format("%s OF %s (%d)", 
                Rank.name(), Suit.name(), getValue());
    }
}
