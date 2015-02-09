package distributedpontoon.client;

import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    private final JPanel cardPanel, buttonPanel, infoPanel;
    private final JButton[] menuButtons;
    private final JButton[] playButtons;
    private final JLabel playerScore, playerBal, playerBet;
    private final ArrayList<JButton> dealerCards;
    private final HashMap<Card, ImageIcon> cardIcons;
    private boolean turnTaken;
    
    public ClientGUI(IPlayer player)
    {
        this.player = player;
        this.dealerCards = new ArrayList<>();
        this.cardIcons = new HashMap<>();
        
        String path = "/distributedpontoon/client/assets/";
        for (Card c : Card.values()) {
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
        
        JButton joinGame = new JButton("Play Game");
        joinGame.setSize(100, 50);
        joinGame.addActionListener(new ButtonListener());
        joinGame.setVisible(true);
        
        JButton changeBet = new JButton("Change Bet");
        changeBet.setSize(100, 50);
        changeBet.addActionListener(new ButtonListener());
        changeBet.setVisible(true);
        
        JButton quit = new JButton("Quit");
        quit.setSize(100, 50);
        quit.addActionListener(new ButtonListener());
        quit.setVisible(true);
        
        this.menuButtons = new JButton[] { joinGame, changeBet, quit };
        
        JButton twistButton = new JButton("Twist");
        twistButton.setSize(100, 50);
        twistButton.addActionListener(new ButtonListener());
        twistButton.setVisible(false);
        
        JButton stickButton = new JButton("Stick");
        stickButton.setSize(100, 50);
        stickButton.addActionListener(new ButtonListener());
        stickButton.setVisible(false);
        
        this.playButtons = new JButton[] { twistButton, stickButton, quit };
        
        this.playerScore = new JLabel("Player Score: 0");
        this.playerScore.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        this.playerScore.setHorizontalAlignment(JLabel.CENTER);
        this.playerScore.setSize(this.getSize().width, 70);
        
        this.playerBal = new JLabel(String.format("Player Balance: %d", 
                player.getBalance()));
        this.playerBal.setSize(this.getSize().width, 70);
        
        this.playerBet = new JLabel("Bet: 0");
        this.playerBet.setSize(this.getSize().width, 70);
        
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(new FlowLayout());
        this.add(cardPanel, BorderLayout.CENTER);
        
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new FlowLayout());
        for (JButton button : menuButtons)
            this.buttonPanel.add(button);
        for (JButton button : playButtons)
            this.buttonPanel.add(button);
        this.buttonPanel.setSize(this.getSize().width, 100);
        this.add(buttonPanel, BorderLayout.NORTH);
        
        this.infoPanel = new JPanel();
        this.infoPanel.setLayout(new BorderLayout());
        this.infoPanel.setSize(this.getSize().width, 100);
        this.infoPanel.add(playerBal, BorderLayout.EAST);
        this.infoPanel.add(playerScore, BorderLayout.CENTER);
        this.infoPanel.add(playerBet, BorderLayout.WEST);
        this.add(infoPanel, BorderLayout.SOUTH);
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
            JButton cardButton = new JButton();
            ImageIcon icon = cardIcons.get(card);
            cardButton.setIcon(icon);
            cardButton.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            cardButton.setVisible(true);
            cardButton.setActionCommand(card.getName());
            cardButton.setBorderPainted(false);
            cardButton.setContentAreaFilled(false);
            cardButton.setBorder(null);
            cardButton.setMargin(new Insets(0, 0, 0, 0));
            if (card.Rank == CardRank.ACE) {
                cardButton.setEnabled(true);
                cardButton.addActionListener(new ActionListener() 
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        for (Card c : game.getHand().getCards()) {
                            if (c.getName().equals(ae.getActionCommand())) {
                                c.setAceHigh(!c.isAceHigh());
                                playerScore.setText(String.format(
                                        "Player Score: %d", 
                                        game.getHand().total())
                                );
                                return;
                            }
                        }
                    }
                });
            }
            cardButton.setBackground(Color.YELLOW);
            cardPanel.add(cardButton);
        }
        playerScore.setText(String.format("Player Score: %d", hand.total()));
        cardPanel.updateUI();
    }
    
    public void joinGame()
    {
        player.startGame();
        for (JButton button : menuButtons)
            button.setVisible(false);
        setHand(game.getHand());
        playerBet.setText(String.format("Bet: %d", game.getBet()));
    }
    
    public void readyTurn()
    {
        System.out.println("Take your turn asshole");
        for (JButton button : playButtons)
            button.setVisible(true);
        turnTaken = false;
    }
    
    public void endTurn()
    {
        System.out.println("Turns over.");
        for (JButton button : playButtons)
            button.setVisible(false);
    }
    
    public synchronized boolean isTurnTaken() { return turnTaken; }
    
    public void leaveGame()
    {
        setHand(new Hand());
        for (JButton button : menuButtons)
            button.setVisible(true);
        playerBal.setText(String.format("Player Balance: %d", 
                player.getBalance()));
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
                case "Change Bet":
                    String answ = JOptionPane.showInputDialog(
                            null, 
                            "Enter you new bet:", 
                            "Change Bet", 
                            JOptionPane.QUESTION_MESSAGE);
                    try {
                        int newBet = Integer.parseInt(answ);
                        game.setBet(newBet);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, 
                                "Error", 
                                "You can only enter numbers!", 
                                JOptionPane.WARNING_MESSAGE);
                    }
                    break;
                case "Quit":
                    leaveGame();
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
            turnTaken = true;
        }   
    }
}
