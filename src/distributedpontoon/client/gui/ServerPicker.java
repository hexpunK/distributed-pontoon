package distributedpontoon.client.gui;

import distributedpontoon.client.IPlayer;
import distributedpontoon.shared.IClientGame;
import distributedpontoon.shared.Pair;
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
 *
 * @author Jordan
 */
public class ServerPicker extends JDialog
{
    private final IPlayer player;
    private final IClientGame game;
    private final ClientGUI owner;
    private JPanel scrollList;
    
    public ServerPicker(IPlayer player, IClientGame game, ClientGUI owner)
    {
        this.player = player;
        this.game = game;
        this.owner = owner;
    }
    
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
    
    public final void updateServers()
    {
        Set<Pair<String, Integer>> servers = player.findServers();
        JScrollPane scroller;
        scrollList = new JPanel(new GridLayout(0, 1));
        if (servers == null || servers.isEmpty()) {
            JLabel label = new JLabel("No servers found.");
            scrollList.add(label, BorderLayout.CENTER);
        } else {
            for (Pair<String, Integer> server : servers) {
                String text = String.format("%s:%d", server.Left, server.Right);
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
    
    private final class ObjectButton<T> extends JButton
    {
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
        
        public final T getItem() { return this.item; }
    }
    
    private final class ButtonHandler implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            ObjectButton<Pair<String, Integer>> source;
            source = ((ObjectButton)e.getSource());
            String server = source.getItem().Left;
            int port = source.getItem().Right;
            owner.setServer(server, port);
            ServerPicker.this.dispose();
        }   
    }
}
