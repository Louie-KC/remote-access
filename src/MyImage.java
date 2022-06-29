import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Written to avoid transmitting ImageIcons over a socket due to the lack
 * of support in future versions of Swing. Additionally avoids using java.beans
 * XMLDecoder (and XMLEncoder) which contains a well known security flaw.
 */
public class MyImage implements Serializable {
    private byte[] data;

    public MyImage(byte[] imageData) {
        data = imageData;
    }

    public MyImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            data = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Retrieves the stored image at its original size as an ImageIcon.
     * @return 
     */
    public ImageIcon getImageIcon() {
        return new ImageIcon(data);
    }

    public ImageIcon getImageIcon(int width, int height) {
        Image img = getImageIcon().getImage();
        return new ImageIcon(img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
    }

    public BufferedImage getBufferedImage() {
        try {
            BufferedImage temp = ImageIO.read(new ByteArrayInputStream(data));
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
