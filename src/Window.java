import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.Dimension;

public class Window extends JFrame implements ActionListener {
    private Base base;
    private JPanel panel;
    private MyImage screenImage;

    private JMenuBar menuBar;
    private final String EXIT_BUTTON_TEXT = "Exit";
    private final String SEND_FILE = "Send file";
    
    public Window(Client c, InputReader inputReader) {
        super("Remote Access window test");
        base = c;
        buildMenu();
        panel = new JPanel();
        setContentPane(panel);
        resizeWindow();
        setVisible(true);
        pack();

        // Set input listeners
        panel.addMouseListener(inputReader);  // panel mouselistener for accurate click position
        addKeyListener(inputReader);  // Frame takes the key listener, does not work on panel
        addMouseWheelListener(inputReader);

        paintAll(getGraphics());  // Draw menubar of items (not menus) without clicking
    }

    @Override
    public void paint(Graphics g) {
        if (screenImage == null) { return; }
        panel.getGraphics().drawImage(screenImage.getBufferedImage(), 0, 0, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {  // Menu item actioning
        switch (e.getActionCommand()) {
            case SEND_FILE:
                int newPort = base.socket.getPort() + 1;
                base.sendMsg(new Message<Integer>(Message.Type.FILE_INIT, newPort));
                new Thread(new FileSender(base.socket.getInetAddress().getHostAddress(),
                    newPort, new File("./.gitignore"))).run();
                break;
            case EXIT_BUTTON_TEXT:
                base.sendMsg(new Message<String>(Message.Type.EXIT, ""));
                base.closeConnection();
                System.exit(0);
        }
    }

    /**
     * Updates the screen image being/to be displayed by the Windows JPanel.
     * @param newImg
     */
    public void setScreenImage(MyImage newImg) { screenImage = newImg; }

    public JPanel getPanel() { return panel; }

    public void resizeWindow() {
        if (base instanceof Client) {
            int reqWidth = ((Client) base).getRequestImgWidth();
            int reqHeight = ((Client) base).getRequestImgHeight();
            panel.setPreferredSize(new Dimension(reqWidth, reqHeight));
            panel.setMaximumSize(new Dimension(reqWidth, reqHeight));
        }
        paintComponents(getGraphics());
    }

    /** Builds and sets the menu bar along the top of the JFrame */
    private void buildMenu() {
        menuBar = new JMenuBar();
        menuBar.add(createMenuButton(SEND_FILE));
        menuBar.add(createMenuButton(EXIT_BUTTON_TEXT));
        setJMenuBar(menuBar);
        menuBar.setVisible(true);
    }

    /**
     * Creates and returns a menu button (JMenuItem) with the specified text, adding the Window
     * instance as its action listener.
     * @param buttonText
     * @return JMenuItem with specified text and listener
     */
    private JMenuItem createMenuButton(String buttonText) {
        JMenuItem button = new JMenuItem(buttonText);
        button.addActionListener(this);
        return button;
    }
}
