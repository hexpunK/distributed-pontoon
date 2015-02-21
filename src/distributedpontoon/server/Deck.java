package distributedpontoon.server;

import distributedpontoon.shared.Card;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * A collection of {@link Card}s, containing all the required cards for a
 * complete standard playing deck.
 *
 * @author 6266215
 * @version 1.0
 * @since 2015-02-03
 */
public class Deck
{
    /** The {@link Card}s left in this {@link Deck}. */
    private final Stack<Card> cards;

    /**
     * Creates a new {@link Deck} of {@link Card} objects and shuffles
     * them. The Cards in this Deck may be popped off the deck and used, but
     * cannot be placed back into the {@link Deck} once removed.
     *
     * @since 1.0
     */
    public Deck()
    {
        this.cards = new Stack<>();
        createDeck();
        shuffle();
    }

    /**
     * Creates a new {@link Deck} of {@link Card}s. The cards will be ordered by
     *  suit and rank once created, so for use in most games will require
     * shuffling through the {@link Deck#shuffle()} method.
     *
     * @since 1.0
     */
    public final void createDeck() 
    { 
        cards.addAll(Card.ALL_CARDS);
    }

    /**
     * Randomises the order of the {@link Card}s in this {@link Deck}.
     *
     * @since 1.0
     */
    public final void shuffle() { Collections.shuffle(cards); }

    /**
     * Removes a {@link Card} from the top of this {@link Deck}.
     *
     * @return A {@link Card} object popped off the the {@link Deck}.
     * @throws DeckException Thrown if the {@link Deck} has no more cards.
     * @since 1.0
     */
    public final Card pullCard() throws DeckException
    {
        try {
            return cards.pop();
        } catch (EmptyStackException ex) {
            throw new DeckException("Empty deck.", ex);
        }
    }

    /**
     * Gets the number of {@link Card}s left in this {@link Deck}.
     *
     * @return The number of {@link Card}s remaining as an int.
     * @since 1.0
     */
    public final int size() { return cards.size(); }
    
    /**
     * Checks whether this {@link Deck} has {@link Card}s still or not.
     *
     * @return Returns true if this {@link Deck} has at least one {@link
     * Card}, false otherwise.
     * @since 1.0
     */
    public final boolean isEmpty() { return cards.isEmpty(); }

    /**
     * Removes all the {@link Card}s currently in this {@link Deck}.
     *
     * @return Returns the number of cards discarded from this {@link Deck} as
     * an int.
     * @since 1.0
     */
    public final int clearDeck()
    {
        int count = cards.size();
        cards.clear();
        return count;
    }

    /**
     * Creates some textual data about this {@link Deck}. If there are {@link
     * Card}s stored, the number and details of them will be given. Otherwise a
     * message about the {@link Deck} being empty will be given instead.
     *
     * @return Returns a String containing the details of the contents for this
     * {@link Deck}.
     * @since 1.0
     * @see Object#toString() 
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (this.isEmpty()) return "This deck is empty.";

        sb.append(String.format("Deck contains %d cards.\n", this.size()));
        sb.append("Remaining cards:");
        for (Card c : cards) {
            sb.append("\n").append("\t").append(c).append(",");
        }

        return sb.toString();
    }

    /**
     * An {@link Exception} thrown by the {@link Deck} class when something goes
     *  wrong with the deck. A {@link DeckException} must have a String message
     * at a minimum, with an optional {@link Throwable} to indicate what caused
     * the error.
     *
     * @version 1.0
     * @since 2015-02-04
     */
    public static final class DeckException extends Exception
    {
        /**
         * Create a new DeckException with the specified error message.
         *
         * @param message The error message to store in this DeckException as a
         * String
         * @since 1.0
         */
        public DeckException(String message) {
            super(message);
        }

        /**
         * Create a new DeckException with the specified error message and
         * reason for the exception being thrown.
         *
         * @param message The error message to store in this DeckException as a
         * String.
         * @param cause The reason for this DeckException as a {@link Throwable}
         *  object.
         * @since 1.0
         */
        public DeckException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
