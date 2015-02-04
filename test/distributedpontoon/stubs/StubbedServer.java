/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributedpontoon.stubs;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Card.CardSuit;
import distributedpontoon.shared.NetMessage;
import distributedpontoon.shared.NetMessage.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Jordan
 */
public class StubbedServer implements Runnable {
    
    private int port = 50000;
    private String hostName;
    private Socket socket;
    private ServerSocket server;

    public StubbedServer()
    {
        this.port = 50000;
        this.hostName = "UNKNOWN";
        this.socket = null;
    }
    
    public StubbedServer(int port)
    {
        this.port = port;
        this.hostName = "UNKNOWN";
        this.socket = null;
    }
    
    public boolean init()
    {
        try {
            server = new ServerSocket(port);
            socket = server.accept();
            hostName = socket.getInetAddress().getHostName();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            return false;
        }
        
        System.out.printf("Stubbed server now running on %s:%d...\n", hostName, port);
        return true;
    }
    
    public void kill()
    {
        try {
            socket.close();
            server.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
    
    @Override
    public void run() 
    {
        if (!init()) return;
        
        System.out.println("Listening...");
        
        ObjectInputStream input;
        ObjectOutputStream output;
        NetMessage<?> msg;
        try {
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
            return;
        }
            
        while (socket.isBound())
        {
            try {
                try {
                    msg = (NetMessage)input.readObject();
                } catch (IOException ex) {
                    continue;
                }
                switch(msg.Type) {
                    case GAME_INITIALISE:
                        Card c1 = new Card(CardSuit.CLUBS, CardRank.TWO);
                        Card c2 = new Card(CardSuit.CLUBS, CardRank.THREE);
                        NetMessage<Card> firstCard = new NetMessage<>(MessageType.CARD_TRANSFER, c1);
                        NetMessage<Card> secondCard = new NetMessage<>(MessageType.CARD_TRANSFER, c2);
                        output.writeObject(firstCard);
                        output.writeObject(secondCard);
                        output.flush();
                        break;
                    case HAND_TRANSFER:
                        System.out.println("Recieved a hand.");
                        break;
                    case TURN_RESPONSE:
                        Character move = (Character)msg.Contents;
                        switch (move) {
                            case 't':
                                Card c = new Card(CardSuit.CLUBS, CardRank.ACE);
                                NetMessage<Card> response = new NetMessage<>(MessageType.CARD_TRANSFER, c);
                                break;
                            case 's':
                                break;
                            case 'b':
                                break;
                            default:
                        }
                        break;
                    default:
                        System.err.printf("StubbedServer does not understand %s messages.", msg.Type);
                }
            } catch (ClassNotFoundException cnfEx) {
                System.err.println(cnfEx.getMessage());
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            }
        }
        
        try {
            socket.close();
        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        }
    }
}
