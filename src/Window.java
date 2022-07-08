import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Dimension;

public class Window extends JFrame {
    private Base base;
    private JPanel panel;
    private MyImage screenImage;
    
    public Window(Client c, InputReader inputReader) {
        super("Remote Access window test");
        base = c;
        panel = new JPanel();
        setContentPane(panel);
        resizeWindow();
        setVisible(true);
        pack();

        // Set listeners
        panel.addMouseListener(inputReader);  // panel mouselistener for accurate click position
        addKeyListener(inputReader);  // Frame takes the key listener, does not work on panel
        addMouseWheelListener(inputReader);
    }

    @Override
    public void paint(Graphics g) {
        if (screenImage == null) { return; }
        panel.getGraphics().drawImage(screenImage.getBufferedImage(), 0, 0, null);

    }

    /**
     * Updates the screen image being/to be displayed by the Windows JPanel.
     * @param newImg
     */
    public void setScreenImage(MyImage newImg) {
        screenImage = newImg;
    }

    public JPanel getPanel() { return panel; }

    public void resizeWindow() {
        if (base instanceof Client) {
            int reqWidth = ((Client) base).getRequestImgWidth();
            int reqHeight = ((Client) base).getRequestImgHeight();
            panel.setPreferredSize(new Dimension(reqWidth, reqHeight));
            panel.setMaximumSize(new Dimension(reqWidth, reqHeight));
        }
    }
}
