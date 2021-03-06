import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

public class Window extends JFrame implements ActionListener {
    static final String EXIT_BUTTON_TEXT = "Exit";
    static final String SEND_FILE = "Send file";
    static final String RECEIVE_FILE = "Receive file";
    static final String SEND_CLIPBOARD = "Send/paste clipboard text";
    static final String GET_CLIPBOARD = "Get remote clipboard text";

    private Base base;
    private JPanel panel;
    private MyImage screenImage;
    private JMenuBar menuBar;
    
    public Window(Base c, String frameTitle) {
        super(frameTitle);
        base = c;
        // Create frames menubar
        menuBar = new JMenuBar();
        menuBar.setVisible(true);
        setJMenuBar(menuBar);
        setVisible(true);
    }

    public Window(Base c, String frameTitle, InputReader inputReader) {
        this(c, frameTitle);
        // Setup JPanel with Window JFrame
        panel = new JPanel();
        setContentPane(panel);
        resizeWindow();
        pack();

        // Set input listeners
        panel.addMouseListener(inputReader);  // panel mouselistener for accurate click position
        addKeyListener(inputReader);  // Frame takes the key listener, does not work on panel
        addMouseWheelListener(inputReader);
        inputReader.setPanel(panel);  // Mouse position listening
        new Thread(inputReader).start();  // Mouse position sending
    }

    @Override
    public void paint(Graphics g) {
        menuBar.repaint();
        if (screenImage == null) { return; }
        panel.getGraphics().drawImage(screenImage.getBufferedImage(), 0, 0, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {  // Menu item actioning
        switch (e.getActionCommand()) {
            case SEND_FILE:
                base.beginFileSending();
                break;
            case RECEIVE_FILE:
                base.sendMsg(new Message<String>(Message.Type.FILE_REQ, ""));
                break;
            case SEND_CLIPBOARD:
                base.sendClipboardText();
                break;
            case GET_CLIPBOARD:
                base.sendMsg(new Message<String>(Message.Type.CLIPBOARD_REQUEST, ""));
                break;
            case EXIT_BUTTON_TEXT:
                base.closeConnection();
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

    /**
     * Adds a menu button (JMenuItem) to the Window JFrame.
     * @param buttonText
     */
    public void addMenuButton(String buttonText) {
        JMenuItem item = new JMenuItem(buttonText);
        item.addActionListener(this);
        menuBar.add(item);
    }
}
