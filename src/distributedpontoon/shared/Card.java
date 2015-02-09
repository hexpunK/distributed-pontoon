package distributedpontoon.shared;

import java.io.Serializable;

/**
 * Represents a standard playing card. Cards can have values ranging from Ace to
 *  King (represented in {@link CardRank}), and must be one of the suits 
 * (represented in {@link CardSuit}). This enumeration contains a reference to 
 * each possible card as of version 1.2.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-09
 */
public enum Card implements Serializable
{    
    ACE_OF_SPADES(CardSuit.SPADES, Card.CardRank.ACE),
    TWO_OF_SPADES(CardSuit.SPADES, Card.CardRank.TWO),
    THREE_OF_SPADES(CardSuit.SPADES, Card.CardRank.THREE),
    FOUR_OF_SPADES(CardSuit.SPADES, Card.CardRank.FOUR),
    FIVE_OF_SPADES(CardSuit.SPADES, Card.CardRank.FIVE),
    SIX_OF_SPADES(CardSuit.SPADES, Card.CardRank.SIX),
    SEVEN_OF_SPADES(CardSuit.SPADES, Card.CardRank.SEVEN),
    EIGHT_OF_SPADES(CardSuit.SPADES, Card.CardRank.EIGHT),
    NINE_OF_SPADES(CardSuit.SPADES, Card.CardRank.NINE),
    TEN_OF_SPADES(CardSuit.SPADES, Card.CardRank.TEN),
    JACK_OF_SPADES(CardSuit.SPADES, Card.CardRank.JACK),
    QUEEN_OF_SPADES(CardSuit.SPADES, Card.CardRank.QUEEN),
    KING_OF_SPADES(CardSuit.SPADES, Card.CardRank.KING),
    ACE_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.ACE),
    TWO_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.TWO),
    THREE_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.THREE),
    FOUR_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.FOUR),
    FIVE_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.FIVE),
    SIX_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.SIX),
    SEVEN_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.SEVEN),
    EIGHT_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.EIGHT),
    NINE_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.NINE),
    TEN_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.TEN),
    JACK_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.JACK),
    QUEEN_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.QUEEN),
    KING_OF_HEARTS(CardSuit.HEARTS, Card.CardRank.KING),
    ACE_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.ACE),
    TWO_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.TWO),
    THREE_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.THREE),
    FOUR_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.FOUR),
    FIVE_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.FIVE),
    SIX_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.SIX),
    SEVEN_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.SEVEN),
    EIGHT_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.EIGHT),
    NINE_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.NINE),
    TEN_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.TEN),
    JACK_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.JACK),
    QUEEN_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.QUEEN),
    KING_OF_CLUBS(CardSuit.CLUBS, Card.CardRank.KING),
    ACE_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.ACE),
    TWO_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.TWO),
    THREE_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.THREE),
    FOUR_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.FOUR),
    FIVE_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.FIVE),
    SIX_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.SIX),
    SEVEN_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.SEVEN),
    EIGHT_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.EIGHT),
    NINE_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.NINE),
    TEN_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.TEN),
    JACK_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.JACK),
    QUEEN_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.QUEEN),
    KING_OF_DIAMONDS(CardSuit.DIAMONDS, Card.CardRank.KING);
    
    /**
     * An enumeration of the valid values for cards in a standard playing card 
     * deck. Each enumerated value has an integer value corresponding to the 
     * point value of the card.
     * 
     * @version 1.0
     * @since 2015-02-03
     */
    public static enum CardRank implements Serializable
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
        /** Serialisation ID. */
        private static final long serialVersionUID = 1L;
        
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
    public static enum CardSuit implements Serializable
    {
        DIAMONDS,
        HEARTS,
        SPADES,
        CLUBS;
        
        /** Serialisation ID. */
        private static final long serialVersionUID = 1L;
    }
    
    /** Serialisation ID. */
    private static final long serialVersionUID = 1L;
    /** The suit of this card as a {@link CardSuit} value. */
    public transient final CardSuit Suit;
    /** The point value of the card as a {@link CardRank}. */
    public transient final CardRank Rank;
    /** If the current card is an Ace, we can make it count as 11 instead. */
    private boolean aceHigh;
    
    /**
     * Creates a new Card with the specified suit and point value.
     * 
     * @param suit The suit of this Card as a {@link CardSuit} value.
     * @param rank The point value of this Card as a {@link CardRank} value.
     * @since 1.0
     */
    private Card(CardSuit suit, CardRank rank)
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
     * Gets the name of this card from its {@link CardRank} and {@link CardSuit}
     *  , separated by "of". The name will be capitalised.
     * 
     * @return Returns the name of this {@link Card} as a {@link String}.
     * @since 1.1
     */
    public String getName()
    {
        return String.format("%s OF %s", Rank.name(), Suit.name());
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
        return String.format("%s (%d)", getName(), getValue());
    }
}
