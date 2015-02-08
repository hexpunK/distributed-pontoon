package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author jwoerner
 */
public class ClientGUI extends JFrame
{
    private IClientGame game;
    private final IPlayer player;
    private final JPanel cardPanel, buttonPanel;
    private final JButton joinGame;
    private final JButton twistButton;
    private final JButton stickButton;
    private final ArrayList<JButton> playerCards;
    private final ArrayList<JButton> dealerCards;
    
    public ClientGUI(IPlayer player)
    {
        this.player = player;
        this.playerCards = new ArrayList<>();
        this.dealerCards = new ArrayList<>();
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println("Couldn't find platform look and feel.");
        }
        
        this.setTitle("Pontoon Client");
        this.setSize(new Dimension(500, 200));
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setLocationByPlatform(true);
        this.setVisible(true);
        
        this.joinGame = new JButton("Play Game");
        this.joinGame.setSize(100, 50);
        this.joinGame.addActionListener(new ButtonListener());
        this.joinGame.setVisible(true);
        
        this.twistButton = new JButton("Twist");
        this.twistButton.setSize(100, 50);
        this.twistButton.addActionListener(new ButtonListener());
        this.twistButton.setVisible(false);
        
        this.stickButton = new JButton("Stick");
        this.stickButton.setSize(100, 50);
        this.stickButton.addActionListener(new ButtonListener());
        this.stickButton.setVisible(false);
        
        buttonPanel = new JPanel(new FlowLayout());
        cardPanel = new JPanel(new FlowLayout());
        
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(cardPanel, BorderLayout.SOUTH);
        
        buttonPanel.add(joinGame);
        buttonPanel.add(twistButton);
        buttonPanel.add(stickButton);
    }
    
    public void setGame(IClientGame game)
    {
        this.game = game;
    }
    
    public void setHand(Hand hand)
    {
        playerCards.clear();
        for (Card card : hand.getCards()) {
            JButton cardButton = new JButton(card.toString());
            cardButton.setVisible(true);
            cardButton.setSize(50, 50);
            playerCards.add(cardButton);
            cardPanel.add(cardButton);
        }
    }
    
    public void joinGame()
    {
        player.startGame();
        this.joinGame.setVisible(false);
        this.joinGame.setEnabled(false);
        this.twistButton.setVisible(true);
        this.stickButton.setVisible(true);
    }
    
    public void readyTurn()
    {
        setHand(game.getHand());
        System.out.println("Take your turn asshole");
        this.twistButton.setEnabled(true);
        this.stickButton.setEnabled(true);
    }
    
    public void endTurn()
    {
        System.out.println("Turns over.");
        this.twistButton.setEnabled(false);
        this.stickButton.setEnabled(false);
    }
    
    public void leaveGame()
    {
        setHand(new Hand());
        this.twistButton.setVisible(false);
        this.stickButton.setVisible(false);
        this.joinGame.setVisible(true);
        this.joinGame.setEnabled(true);
    }
    
    private final class ButtonListener implements ActionListener
    {   
        @Override
        public void actionPerformed(ActionEvent ae) 
        {
            switch (ae.getActionCommand()) {
                case "Play Game":
                    joinGame();
                    break;
                case "Twist":
                    game.twist();
                    break;
                case "Stick":
                    game.stand();
                    break;
                default:
                    System.out.println(ae.getActionCommand());
            }
            endTurn();
        }   
    }
}
