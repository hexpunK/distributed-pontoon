package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
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
    private final ArrayList<JButton> dealerCards;
    private final HashMap<Card, ImageIcon> cardIcons;
    
    public ClientGUI(IPlayer player)
    {
        this.player = player;
        this.dealerCards = new ArrayList<>();
        this.cardIcons = new HashMap<>();
        
        String path = "/distributedpontoon/client/assets/";
        for (Card c : Card.ALL_CARDS) {
            ImageIcon img = createImageIcon(path+c.getName()+".png", c.getName());
            cardIcons.put(c, img);
        }
        
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
        buttonPanel.setBackground(Color.RED);
        cardPanel = new JPanel(new FlowLayout());
        cardPanel.setBackground(Color.BLUE);
        cardPanel.setSize(500, 100);
        
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(cardPanel, BorderLayout.CENTER);
        
        buttonPanel.add(joinGame);
        buttonPanel.add(twistButton);
        buttonPanel.add(stickButton);
    }
    
    private ImageIcon createImageIcon(String path, String description)
    {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public void setGame(IClientGame game)
    {
        this.game = game;
    }
    
    public void setHand(Hand hand)
    {
        cardPanel.removeAll();
        for (Card card : hand.getCards()) {
            JButton cardButton = new JButton(cardIcons.get(card));
            cardButton.setVisible(true);
            if (card.Rank == CardRank.ACE)
                cardButton.setEnabled(true);
            else
                cardButton.setEnabled(false);
            cardButton.setOpaque(false);
            cardButton.setContentAreaFilled(true);
            cardButton.setBackground(Color.YELLOW);
            cardButton.setBorderPainted(false);
            cardButton.setFocusPainted(false);
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
        setHand(game.getHand());
    }
    
    public void readyTurn()
    {
        System.out.println("Take your turn asshole");
        this.twistButton.setEnabled(true);
        this.stickButton.setEnabled(true);
    }
    
    public void endTurn()
    {
        System.out.println("Turns over.");
        this.twistButton.setEnabled(false);
        this.stickButton.setEnabled(false);
        setHand(game.getHand());
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
