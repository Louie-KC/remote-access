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

    private Base base;
    private JPanel panel;
    private MyImage screenImage;
    private JMenuBar menuBar;
    
    public Window(Client c, InputReader inputReader) {
        super("Remote Access window test");
        base = c;
        // Create frames menubar
        menuBar = new JMenuBar();
        menuBar.setVisible(true);
        setJMenuBar(menuBar);

        // Setup JPanel with Window JFrame
        panel = new JPanel();
        setContentPane(panel);
        resizeWindow();
        setVisible(true);
        pack();

        // Set input listeners
        panel.addMouseListener(inputReader);  // panel mouselistener for accurate click position
        addKeyListener(inputReader);  // Frame takes the key listener, does not work on panel
        addMouseWheelListener(inputReader);
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
