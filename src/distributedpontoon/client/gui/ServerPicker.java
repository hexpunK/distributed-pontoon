package distributedpontoon.client.gui;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.IServerGame;
import distributedpontoon.shared.Triple;
import distributedpontoon.server.Server;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Gets and displays a list of known {@link Server}s and {@link IServerGame}s, 
 * allowing a {@link IPlayer} to select the server to play on.
 * 
 * @author 6266215
 * @version 1.0
 * @since 2015-02-12
 * @see JDialog
 */
public class ServerPicker extends JDialog
{
    private final IPlayer player;
    private final ClientGUI owner;
    private JPanel scrollList;
    
    /**
     * Creates a new {@link ServerPicker}, this will not display the GUI or get 
     * the server listing, to do this call {@link ServerPicker#updateServers()}.
     * 
     * @param player The {@link IPlayer} to set the server details for.
     * @param owner The {@link ClientGUI} to set the server details for.
     * @since 1.0
     */
    public ServerPicker(IPlayer player, ClientGUI owner)
    {
        this.player = player;
        this.owner = owner;
    }
    
    /**
     * Sets up the GUI elements for this {@link ServerPicker}. This will make 
     * the {@link JDialog} a full modal, preventing it from losing focus to the 
     * owning {@link ClientGUI}.
     * 
     * @since 1.0
     */
    public final void initGUI()
    {
        this.setLayout(new BorderLayout());
        this.setTitle("Server Browser");
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setLocationByPlatform(true);
        this.setPreferredSize(new Dimension(250, 150));
        this.setResizable(false);
        
        JLabel title = new JLabel("Servers", null, JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(title, BorderLayout.NORTH);
        updateServers();
        this.setVisible(true);
    }
    
    /**
     * Updates the server listing.
     * 
     * @since 1.0
     */
    public final void updateServers()
    {
        Set<Triple<String, Integer, Integer>> servers = player.findServers();
        JScrollPane scroller;
        scrollList = new JPanel(new GridLayout(0, 1));
        if (servers == null || servers.isEmpty()) {
            JLabel label = new JLabel("No servers found.");
            scrollList.add(label, BorderLayout.CENTER);
        } else {
            for (Triple<String, Integer, Integer> server : servers) {
                String gameType = String.format("Game %d", server.Three);
                if (server.Three == -1)
                    gameType = "New Single Game";
                else if (server.Three == 0)
                    gameType = "New Multi-player Game";
                String text = String.format("%s:%d - %s", 
                        server.One, server.Two, gameType);
                ObjectButton<?> button = new ObjectButton<>(server, text);
                button.addActionListener(new ButtonHandler());
                button.setSize(scrollList.getWidth(), 50);
                button.setVisible(true);
                scrollList.add(button);
            }
        }
        scroller = new JScrollPane(scrollList);
        scroller.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroller.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );
        this.add(scroller, BorderLayout.CENTER);
        this.pack();
    }
    
    /**
     * A button that can hold an {@link Object} instance, allowing it to be 
     * pulled out when needed. Other than this, {@link ObjectButton} behaves 
     * like a normal {@link JButton}.
     * 
     * @param <T> The type of the item to store.
     * @version 1.0
     * @since 1.0
     * @see JButton
     */
    private final class ObjectButton<T> extends JButton
    {
        /** The item to hold in this button. */
        private final T item;

        public ObjectButton()
        {
            super();
            this.item = null;
        }

        public ObjectButton(Action a)
        { 
           super(a);
           this.item = null; 
        }

        public ObjectButton(Icon icon)
        {
            super(icon);
            this.item = null;
        }

        public ObjectButton(T item)
        {
            this.item = item;
        }

        public ObjectButton(String text) 
        {
            super(text);
            this.item = null;
        }
        
        public ObjectButton(String text, Icon icon) 
        {
            super(text, icon);
            this.item = null;
        }

        public ObjectButton(T item, Action a) 
        {
            super(a);
            this.item = item;
        }

        public ObjectButton(T item, Icon icon) 
        {
            super(icon);
            this.item = item;
        }

        public ObjectButton(T item, String text) 
        {
            super(text);
            this.item = item;
        }

        public ObjectButton(T item, String text, Icon icon)
        {
            super(text, icon);
            this.item = item;
        }
        
        /**
         * Gets the item stored in this {@link ObjectButton}.
         * 
         * @return The object stored in this ObjectButton as a instance of type 
         * &lt;T&gt;.
         */
        public final T getItem() { return this.item; }
    }
    
    /**
     * Handles interactions with the {@link ObjectButton}s in this 
     * {@link ServerPicker}.
     * 
     * @version 1.0
     * @since 1.0
     */
    private final class ButtonHandler implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            ObjectButton<Triple<String, Integer, Integer>> source;
            source = ((ObjectButton)e.getSource());
            String server = source.getItem().One;
            int port = source.getItem().Two;
            int gameID = source.getItem().Three;
            owner.setGame(server, port, gameID);
            
            ServerPicker.this.dispose();
        }   
    }
}
