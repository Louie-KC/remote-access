import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Dimension;

public class Window extends JFrame {
    Base base;
    JPanel panel;
    MyImage screenImage;
    boolean resizedForImg;
    Graphics panelGraphics;
    
    public Window(Client c, InputReader inputReader) {
        super("Remote Access window test");
        base = c;
        setSize(c.WIDTH, c.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Default, to be changed by client
        addKeyListener(inputReader);  // Frame takes the key listener, does not work on panel
        setLocationByPlatform(true);
        setVisible(true);

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(c.WIDTH, c.HEIGHT));
        panel.addMouseListener(inputReader);  // panel mouselistener for accurate click position
        setContentPane(panel);

        panelGraphics = panel.getGraphics();
        resizedForImg = false;
    }

    @Override
    public void paint(Graphics g) {
        if (screenImage == null) { return; }
        panelGraphics.drawImage(screenImage.getBufferedImage(), 0, 0, null);
        if (!resizedForImg) {
            pack();
            panelGraphics = panel.getGraphics();
            resizedForImg = true;
        }
    }

    /**
     * Updates the screen image being/to be displayed by the Windows JPanel.
     * @param newImg
     */
    public void setScreenImage(MyImage newImg) {
        screenImage = newImg;
    }
}
