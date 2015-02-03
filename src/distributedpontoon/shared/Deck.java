package distributedpontoon.shared;

import java.util.Collections;
import java.util.Stack;

/**
 * A deck of {@link Card}s, containing all the required cards for a complete 
 * deck.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-03
 */
public class Deck {
    
    Stack<Card> cards;
    
    public Deck()
    {
        createDeck();
    }
    
    public final void createDeck()
    {
        clearDeck();
        for (Card.CardSuit suit : Card.CardSuit.values())
            for (Card.CardValue value : Card.CardValue.values())
                cards.add(new Card(suit, value));
        shuffle();
    }
    
    public final void shuffle()
    {
        Collections.shuffle(cards);
    }
    
    public final Card pullCard()
    {
        return cards.pop();
    }
    
    public final void clearDeck()
    {
        this.cards = new Stack<>();
    }
}
