package distributedpontoon.client.gui;

import distributedpontoon.client.ClientGame;
import distributedpontoon.client.GUIPlayer;
import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.Card;
import distributedpontoon.shared.Card.CardRank;
import distributedpontoon.shared.Hand;
import distributedpontoon.shared.IClientGame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
 * A Swing GUI for the {@link GUIPlayer} to use. Displays both the players hand 
 * and the dealers hand, along with details on the players current balance, bet 
 * and score.
 * 
 * @author 6266215
 * @version 1.2
 * @since 2015-02-16
 * @see JFrame
 */
public class ClientGUI extends JFrame
{
    /** This GUI should be a singleton, no point running multiple. */
    private static ClientGUI INSTANCE;
    private final IPlayer player;
    private final JPanel cardPanel, playerCards, dealerCards, buttonPanel, 
            infoPanel;
    private final JButton[] menuButtons;
    private final JButton readyButton;
    private final JButton[] playButtons;
    private final JLabel playerScore, playerBal, playerBet, gameInfo;
    private final HashMap<Card, ImageIcon> cardIcons;
    private final Dimension buttonSize = new Dimension(150, 75);
    
    private IClientGame game;
    private boolean turnTaken;
    
    public static final boolean PLAYER = true;
    public static final boolean DEALER = false;
    
    /**
     * Creates a new instance of the {@link ClientGUI}. Sets up all of the UI 
     * elements and organises them for the default layout and visibility.
     * 
     * @param player The {@link IPlayer} that will interact with this GUI. Used 
     * for getting game details.
     * @since 1.0
     */
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
        
        readyButton = new JButton("Ready");
        readyButton.setSize(buttonSize);
        readyButton.addActionListener(new ButtonListener());
        readyButton.setVisible(false);
        
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
        
        this.playerCards = new JPanel();
        this.playerCards.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        this.dealerCards = new JPanel();
        this.dealerCards.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        this.buttonPanel = new JPanel();
        this.buttonPanel.setLayout(new FlowLayout());
        for (JButton button : menuButtons)
            this.buttonPanel.add(button);
        for (JButton button : playButtons)
            this.buttonPanel.add(button);
        
        this.cardPanel.add(readyButton);
        this.cardPanel.add(gameInfo);
        this.cardPanel.add(playerCards, BorderLayout.SOUTH);
        this.cardPanel.add(dealerCards, BorderLayout.NORTH);
        
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
    
    /**
     * Gets the singleton instance of {@link ClientGUI}, if none exists a new 
     * one is created.
     * 
     * @param player The {@link IPlayer} that will interact with this GUI.
     * @return Returns the singleton instance of {@link ClientGUI}.
     * @since 1.0
     */
    public synchronized static ClientGUI getInstance(IPlayer player)
    {
        if (ClientGUI.INSTANCE == null) {
            ClientGUI.INSTANCE = new ClientGUI(player);
        }
        ClientGUI.INSTANCE.setVisible(true);
        return ClientGUI.INSTANCE;
    }
    
    /**
     * Creates a new {@link ImageIcon} from the specified path and sets the 
     * description in the image to the specified String.
     * 
     * @param path The file path and file name of the image to load.
     * @param description The description of the image.
     * @return A new {@link ImageIcon} instance.
     */
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
    
    /**
     * Sets the {@link IClientGame} this {@link ClientGUI} will connect to.
     * 
     * @param server The host name or IP address of the server as a String.
     * @param port The port to connect to as an int.
     * @param gameID The game ID as an int. If this is -1 or 0 new games will be
     *  created, otherwise an existing game will be joined if one exists.
     */
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
    
    /**
     * Updates the {@link JLabel} that contains the {@link IPlayer}s current bet
     *  , uses the running {@link IClientGame} to get the bet amount.
     * 
     * @since 1.1
     */
    public void updateBet()
    {
        if (game != null)
            playerBet.setText(String.format("Bet: %d", game.getBet()));
        else
            playerBet.setText("Join a game first!");
    }
    
    /**
     * Sets the {@link Hand} used to display the {@link Card}s in the GUI.
     * 
     * @param hand The {@link Hand} to get {@link Card} objects from.
     * @param dealer Set to true if this is the dealers hand, false otherwise.
     * @since 1.0
     */
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
            // Aces need to be interactive buttons.
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
            // Make sure these are up to date.
            dealerCards.updateUI();
            playerCards.updateUI();
        }
    }
    
    /**
     * Connects to the game selected. Removes menu GUI elements and replaces 
     * them with game elements.
     * 
     * @since 1.0
     */
    public void joinGame()
    {
        gameInfo.setVisible(false);
        for (JButton button : menuButtons)
            button.setVisible(false);
        readyButton.setVisible(true);
        playerBet.setText(String.format("Bet: %d", game.getBet()));
        playerBal.setText(String.format("Player Balance: %d", 
                player.getBalance()));
        player.startGame();
    }
    
    /**
     * Tells the game to start running.
     * 
     * @since 1.2
     */
    public void playGame()
    {
        readyButton.setVisible(false);
        for (JButton button : playButtons)
            button.setVisible(true);
        setHand(game.getHand(), PLAYER);
        Hand tmpDlrHand = new Hand();
        tmpDlrHand.addCard(null);
        tmpDlrHand.addCard(null);
        setHand(tmpDlrHand, DEALER);
        playerCards.setVisible(true);
        dealerCards.setVisible(true);
    }
    
    /**
     * Enables the buttons that the player needs to use to take their turn.
     * 
     * @since 1.0
     */
    public void readyTurn()
    {
        for (JButton button : playButtons)
            button.setEnabled(true);
        turnTaken = false;
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    /**
     * Disables the buttons that the player uses to take their turn.
     * 
     * @since 1.0
     */
    public void endTurn()
    {
        setHand(game.getHand(), PLAYER);
        for (JButton button : playButtons)
            button.setEnabled(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    /**
     * Checks to see if the player has taken their turn yet.
     * 
     * @return Returns true if the player has taken their turn, false otherwise.
     * @since 1.0
     */
    public synchronized boolean isTurnTaken() { return turnTaken; }
    
    /**
     * Disconnects from the current game, setting the UI back to the main menu.
     * 
     * @since 1.0
     */
    public void leaveGame()
    {
        this.setCursor(Cursor.getDefaultCursor());
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
    
    /**
     * Handles the various button presses in this GUI.
     * 
     * @version 1.0
     * @since 1.0
     */
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
                            new ServerPicker(player, ClientGUI.INSTANCE);
                    picker.initGUI();
                    break;
                case "Ready":
                    game.startGame();
                    playGame();
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
