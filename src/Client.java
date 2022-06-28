import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.image.BufferedImage;
import java.awt.*;

public class Client extends Base {

    JFrame frame;
    
    public Client(String targetIP, int targetPort) {
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("Client test");
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Testing if reading locks
        System.out.println("Sleeping 5s");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.getMessage();
        }
        sendMsg(new Message<String>("", null));
        if (!receiveMsg()) { System.exit(0); }
        System.out.println("Received  message");

        JLabel imgTest = new JLabel();
        imgTest.setIcon(readScreenImg());
        frame.add(imgTest);
        frame.pack();
    }

    /**
     * Checks for and reads an ImageIcon from the last received message.
     * @return ImageIcon if present, null otherwise.
     */
    private ImageIcon readScreenImg() {
        if (lastMsg != null && lastMsg.getType().equals("img")) {
            if (lastMsg.getData() instanceof ImageIcon) {
                return (ImageIcon)lastMsg.getData();
            }
        }
        System.out.println("readScreenImg: invalid msg");
        return null;
    }

    /**
     * Converts an Image to a BufferedImage. Useful for converted ImageIcons to BufferedImages,
     * as ImageIcons are serialisable and BufferedImages are not.
     * @param image
     * @return BufferedImage instance of image param.
     */
    private BufferedImage imageToBufferedImage(Image image) {
        if (image instanceof BufferedImage) { return (BufferedImage)image; }
        BufferedImage temp = new BufferedImage(image.getWidth(null),
                                                image.getHeight(null),
                                                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = temp.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return temp;
    }


}
