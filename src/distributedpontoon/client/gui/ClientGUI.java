package distributedpontoon.client.gui;

import distributedpontoon.client.ClientGame;
import distributedpontoon.client.IPlayer;
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
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * 
 * 
 * @author 6266215
 * @version 1.0
 */
public class ClientGUI extends JFrame
{
    private static ClientGUI INSTANCE;
    private final IPlayer player;
    private final JPanel cardPanel, playerCards, dealerCards, buttonPanel, 
            infoPanel;
    private final JButton[] menuButtons;
    private final JButton[] playButtons;
    private final JLabel playerScore, playerBal, playerBet, gameInfo;
    private final HashMap<Card, ImageIcon> cardIcons;
    private final Dimension buttonSize = new Dimension(150, 75);
    
    private IClientGame game;
    private boolean turnTaken;
    
    public static final boolean PLAYER = true;
    public static final boolean DEALER = false;
    
    private ClientGUI(IPlayer player)
    {
        this.player = player;
        this.cardIcons = new HashMap<>();
        
        String path = "/distributedpontoon/client/assets/";
        for (Card c : Card.ALL_CARDS) {
            ImageIcon img = createImageIcon(path+c.getName()+".png",
                    c.getName());
            cardIcons.put(c, img);
        }
        ImageIcon noCard = createImageIcon(path+"back.png", "Back");
        cardIcons.put(null, noCard);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException 
                | InstantiationException 
                | IllegalAccessException 
                | UnsupportedLookAndFeelException ex) {
            System.err.println("Couldn't find platform look and feel.");
        }
        
        this.setTitle("Pontoon Client");
        this.setSize(new Dimension(500, 350));
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setLocationByPlatform(true);
        
        JButton joinGame = new JButton("Play Game");
        joinGame.setSize(buttonSize);
        joinGame.addActionListener(new ButtonListener());
        joinGame.setVisible(true);
        
        JButton changeBet = new JButton("Change Bet");
        changeBet.setSize(buttonSize);
        changeBet.addActionListener(new ButtonListener());
        changeBet.setVisible(true);
        
        JButton pickServer = new JButton("Pick Server");
        pickServer.setSize(buttonSize);
        pickServer.addActionListener(new ButtonListener());
        pickServer.setVisible(true);
        
        JButton quit = new JButton("Quit");
        quit.setSize(buttonSize);
        quit.addActionListener(new ButtonListener());
        quit.setVisible(false);
        
        JButton twistButton = new JButton("Twist");
        twistButton.setSize(buttonSize);
        twistButton.addActionListener(new ButtonListener());
        twistButton.setVisible(false);
        
        JButton stickButton = new JButton("Stick");
        stickButton.setSize(buttonSize);
        stickButton.addActionListener(new ButtonListener());
        stickButton.setVisible(false);
        
        this.menuButtons = new JButton[] { joinGame, changeBet, pickServer };
        this.playButtons = new JButton[] { twistButton, stickButton, quit };
        
        this.playerScore = new JLabel("Player Score: 0");
        this.playerScore.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        this.playerScore.setHorizontalAlignment(JLabel.CENTER);
        this.playerScore.setSize(this.getSize().width, 70);
        this.playerScore.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        this.playerBal = new JLabel(String.format("Player Balance: %d", 
                player.getBalance()));
        this.playerBal.setSize(this.getSize().width, 70);
        this.playerBal.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        this.playerBet = new JLabel("Bet: 0");
        this.playerBet.setSize(this.getSize().width, 70);
        this.playerBet.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        this.gameInfo = new JLabel("No game selected.");
        this.gameInfo.setHorizontalAlignment(SwingConstants.CENTER);
        this.gameInfo.setVerticalAlignment(SwingConstants.CENTER);
        this.gameInfo.setSize(this.getSize().width, 70);
        this.gameInfo.setVisible(true);
        
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(new BorderLayout());
        this.cardPanel.add(gameInfo);
        
        this.playerCards = new JPanel();
        this.playerCards.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        this.cardPanel.add(playerCards, BorderLayout.SOUTH);
        
        this.dealerCards = new JPanel();
        this.dealerCards.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        this.cardPanel.add(dealerCards, BorderLayout.NORTH);
        
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new FlowLayout());
        for (JButton button : menuButtons)
            this.buttonPanel.add(button);
        for (JButton button : playButtons)
            this.buttonPanel.add(button);
        
        this.infoPanel = new JPanel();
        this.infoPanel.setLayout(new BorderLayout());
        this.infoPanel.add(playerBal, BorderLayout.EAST);
        this.infoPanel.add(playerScore, BorderLayout.CENTER);
        this.infoPanel.add(playerBet, BorderLayout.WEST);
        this.infoPanel.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
        
        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(cardPanel, BorderLayout.CENTER);
        this.add(infoPanel, BorderLayout.SOUTH);
        this.setVisible(true);
        this.toFront();
    }
    
    public synchronized static ClientGUI getInstance(IPlayer player)
    {
        if (ClientGUI.INSTANCE == null) {
            ClientGUI.INSTANCE = new ClientGUI(player);
        }
        ClientGUI.INSTANCE.setVisible(true);
        return ClientGUI.INSTANCE;
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
    
    public void setGame(String server, int port, int gameID)
    {
        if (game != null)
            game = new ClientGame(player, game.getBet(), server, port);
        else
            game = new ClientGame(player, 50, server, port);
        game.setGameID(gameID);
        player.reigsterGame(game);
        gameInfo.setText(String.format(
                "<html>Connecting to server:<br />%s:%d</html>", 
                server, port));
        updateBet();
    }
    
    public void updateBet()
    {
        if (game != null)
            playerBet.setText(String.format("Bet: %d", game.getBet()));
        else
            playerBet.setText("Join a game first!");
    }
    
    public synchronized void setHand(Hand hand, boolean dealer)
    {
        if (dealer == DEALER)
            dealerCards.removeAll();
        else
            playerCards.removeAll();
        Card[] cards = hand.getCards().toArray(new Card[hand.size()]);
        for (Card card : cards) {
            JButton cardButton = new JButton();
            ImageIcon icon = cardIcons.get(card);
            cardButton.setIcon(icon);
            cardButton.setPreferredSize(
                    new Dimension(
                            icon.getIconWidth(), 
                            icon.getIconHeight()
                    ));
            cardButton.setVisible(true);
            cardButton.setBorderPainted(false);
            cardButton.setContentAreaFilled(false);
            cardButton.setBorder(null);
            cardButton.setMargin(new Insets(0, 0, 0, 0));
            if (dealer == PLAYER && card.Rank == CardRank.ACE) {
                cardButton.setActionCommand(card.getName());
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
            if (dealer == DEALER) {
                dealerCards.add(cardButton);
            } else {
                playerCards.add(cardButton);
                playerScore.setText(String.format("Player Score: %d", 
                        hand.total()));
            }
            dealerCards.updateUI();
            playerCards.updateUI();
        }
    }
    
    public void joinGame()
    {
        playerCards.setVisible(true);
        dealerCards.setVisible(true);
        gameInfo.setVisible(false);
        for (JButton button : menuButtons)
            button.setVisible(false);
        for (JButton button : playButtons)
            button.setVisible(true);
        setHand(game.getHand(), PLAYER);
        playerBet.setText(String.format("Bet: %d", game.getBet()));
        playerBal.setText(String.format("Player Balance: %d", 
                player.getBalance()));
        Hand tmpDlrHand = new Hand();
        tmpDlrHand.addCard(null);
        tmpDlrHand.addCard(null);
        setHand(tmpDlrHand, DEALER);
    }
    
    public void readyTurn()
    {
        for (JButton button : playButtons)
            button.setEnabled(true);
        turnTaken = false;
    }
    
    public void endTurn()
    {
        setHand(game.getHand(), PLAYER);
        for (JButton button : playButtons)
            button.setEnabled(false);
    }
    
    public synchronized boolean isTurnTaken() { return turnTaken; }
    
    public void leaveGame()
    {
        playerCards.setVisible(false);
        dealerCards.setVisible(false);
        gameInfo.setVisible(true);
        cardPanel.add(gameInfo);
        for (JButton button : playButtons)
            button.setVisible(false);
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
                    player.startGame();
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
                        updateBet();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, 
                                "Error", 
                                "You can only enter numbers!", 
                                JOptionPane.WARNING_MESSAGE);
                    }
                    break;
                case "Pick Server":
                    final ServerPicker picker = 
                            new ServerPicker(player, game, ClientGUI.INSTANCE);
                    picker.initGUI();
                    break;
                case "Quit":
                    player.leaveGame(game);
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
