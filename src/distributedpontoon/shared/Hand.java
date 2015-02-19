package distributedpontoon.shared;

import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.IServerGame;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a hand in a playing card game. Contains zero or more {@link Card}
 * objects to be shared between an {@link IClientGame} and {@link IServerGame}.
 *
 * @author 6266215
 * @version 1.0
 * @since 2015-02-03
 */
public class Hand implements Serializable
{
    /** Serialisation ID. */
    private static final long serialVersionUID = 1L;
    /** The {@link Card}s held in this Hand. */
    private final ArrayList<Card> cards;

    /**
     * Creates a new empty {@link Hand} for a playing card game.
     *
     * @since 1.0
     */
    public Hand()
    {
        this.cards = new ArrayList<>();
    }

    /**
     * Adds the provided {@link Card} to this {@link Hand}.
     *
     * @param c The {@link Card} object to store in this {@link Hand}.
     * @since 1.0
     */
    public final void addCard(Card c) { cards.add(c); }

    /**
     * Gets the {@link Card}s held in this {@link Hand}.
     *
     * @return The {@link ArrayList} of {@link Card}s store in this Hand.
     * @since 1.0
     */
    public final ArrayList<Card> getCards() { return cards; }

    /**
     * Gets the number of {@link Card}s held in this {@link Hand}.
     *
     * @return Returns the number of {@link Card} objects stored as an int.
     * @since 1.0
     */
    public final int size() { return cards.size(); }

    /**
     * Checks to see if there are any {@link Card}s in this {@link Hand}.
     *
     * @return Returns true if there is at least one {@link Card} held, returns
     * false otherwise.
     * @since 1.0
     */
    public final boolean isEmpty() { return cards.isEmpty(); }

    /**
     * Calculates the total score of the {@link Card}s held in this hand based
     * on their {@link CardRank} scores.
     *
     * @return The total value of this {@link Hand} as an int.
     * @since 1.0
     */
    public final int total()
    {
        int total = 0;
        for (Card c : cards) {
            total += c.getValue();
        }
        return total;
    }

    /**
     * Removes all the {@link Card}s held in this {@link Hand}.
     *
     * @return The number of cards removed from this hand as an int.
     * @since 1.0
     */
    public final int clear()
    {
        int num = cards.size();
        cards.clear();
        return num;
    }

    /**
     * Creates some textual data about this {@link Hand}. If there are {@link
     * Card}s stored, the number and details of them will be given. Otherwise a
     * message about the {@link Hand} being empty will be given instead.
     *
     * @return Returns a String containing the details of the contents for this
     * {@link Hand}.
     * @since 1.0
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (this.isEmpty()) return "The hand is empty.";

        sb.append(String.format("Hand contains %d cards.\n", this.size()));
        sb.append("Remaining cards:");
        for (Card c : cards) {
            sb.append("\n").append("\t").append(c).append(",");
        }
        sb.append(String.format("\nHand value: %d", total()));

        return sb.toString();
    }
}
