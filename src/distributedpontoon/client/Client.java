package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Deck;
import distributedpontoon.shared.Deck.DeckException;
import distributedpontoon.shared.Hand;
import java.util.Random;

/**
 *
 * @author 6266215
 */
public class Client
{    
    public static void main(String[] args)
    {
        Deck d = new Deck();
        Hand h = new Hand();
        Random ran = new Random();
        int max = ran.nextInt(Card.ALL_CARDS.size());
        for (int i = 0; i < max; i++) {
            try {
                Card c = d.pullCard();
                if (c.Rank == Card.CardRank.ACE && ran.nextBoolean())
                    c.setAceHigh(true);
            
                h.addCard(c);
            } catch (DeckException ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        System.out.println(d);
        System.out.println(h);
    }
}
