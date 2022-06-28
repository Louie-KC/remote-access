import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.awt.*;

public class Client extends Base {
    
    public Client(String targetIP, int targetPort) {
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Testing
        if (!receiveMsg()) { return; }
        try {
            BufferedImage tempImg = imageToBufferedImage(readScreenImg().getImage());
            ImageIO.write(tempImg, "png", new File("./receivedTest.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
