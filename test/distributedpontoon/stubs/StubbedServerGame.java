package distributedpontoon.stubs;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.NetMessage;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Jordan
 */
public class StubbedServerGame implements Runnable {

    private final Socket socket;
    
    public StubbedServerGame(Socket s)
    {
        this.socket = s;
    }
    
    @Override
    public void run() {
        ObjectInputStream input;
        ObjectOutputStream output;
        NetMessage<?> msg;
        Card c;
        
        try {
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
                return;
            }
        
            while (!socket.isClosed()) {
               msg =(NetMessage)input.readObject();
               switch(msg.Type) {
                   case GAME_INITIALISE:
                       Card c1 = new Card(Card.CardSuit.CLUBS, Card.CardRank.TWO);
                       Card c2 = new Card(Card.CardSuit.CLUBS, Card.CardRank.THREE);
                       NetMessage<Card> firstCard = new NetMessage<>(NetMessage.MessageType.CARD_TRANSFER, c1);
                       NetMessage<Card> secondCard = new NetMessage<>(NetMessage.MessageType.CARD_TRANSFER, c2);
                       output.writeObject(firstCard);
                       output.writeObject(secondCard);
                       output.flush();
                       break;
                   case TURN_RESPONSE:
                       Character move = (Character)msg.Contents;
                       switch (move) {
                           case 't':
                               System.out.println("Player has twisted.");
                               c = new Card(Card.CardSuit.CLUBS, Card.CardRank.ACE);
                               NetMessage<Card> response = new NetMessage<>(MessageType.CARD_TRANSFER, c);
                               output.writeObject(response);
                               output.flush();
                               break;
                           case 's':
                               System.out.println("Player has stuck.");
                               NetMessage<Card[]> cardMsg = (NetMessage)input.readObject();
                               Card[] cards = (Card[])cardMsg.Contents;
                               NetMessage<Integer> msg2 = (NetMessage)input.readObject();
                               Hand h = new Hand();
                               for (Card card : cards)
                                   h.addCard(card);

                               NetMessage<Boolean> resp;
                               if (h.total() == msg2.Contents)
                                   resp = new NetMessage<>(MessageType.GAME_RESULT, true);
                               else
                                   resp = new NetMessage<>(MessageType.GAME_RESULT, false);
                               output.writeObject(resp);
                               output.flush();
                               break;
                           case 'b':
                               System.out.println("Player is bust.");
                               msg = new NetMessage<>(MessageType.GAME_RESULT, false);
                               output.writeObject(msg);
                               output.flush();
                               break;
                           default:
                       }
                       break;
                   default:
                       System.err.printf("StubbedServer does not understand '%s' messages.", msg.Type);
               }
            }
        } catch (ClassNotFoundException cnfEx) {
            System.err.println(cnfEx.getMessage());
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException innerIOEx) {
                    System.err.println(innerIOEx.getMessage());
                }
            }
        }
    }
    
}
