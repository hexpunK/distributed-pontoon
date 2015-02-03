package distributedpontoon.shared;

/**
 * Represents a standard playing card. Cards can have values ranging from Ace to
 *  King (represented in {@link CardValue}, and must be one of the suits 
 * represented in {@link CardSuit}. Once created a Card instance is immutable.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-03
 */
public class Card {
    
    /**
     * 
     * 
     * @version 1.0
     * @since 2015-02-03
     */
    public static enum CardValue
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
        
        private final int value;
        CardValue(int points) { this.value = points; }
        public final int getValue() { return this.value; }        
    }
    
    /**
     * 
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
    
    /** The suit of this card as a {@link CardSuit} value. */
    public final CardSuit Suit;
    /** The point value of the card as a {@link CardValue}. */
    public final CardValue Value;
    
    /**
     * Creates a new Card with the specified suit and point value.
     * 
     * @param suit The suit of this Card as a {@link CardSuit} value.
     * @param value The point value of this Card as a {@link CardValue} value.
     * @since 1.0
     */
    public Card(CardSuit suit, CardValue value)
    {
        this.Suit = suit;
        this.Value = value;
    }
}
