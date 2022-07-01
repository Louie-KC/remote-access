import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

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

    public MyImage(ImageIcon image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage buffTemp = new BufferedImage(
                image.getIconWidth(), image.getIconHeight(),
                BufferedImage.TYPE_INT_RGB
            );
            Graphics g = buffTemp.createGraphics();
            g.drawImage(image.getImage(), 0, 0, null);
            ImageIO.write(buffTemp, "png", baos);
            data = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Retrieves the stored image (no resizing) as an ImageIcon.
     * @return 
     */
    public ImageIcon getImageIcon() {
        return new ImageIcon(data);
    }

    /**
     * Retrieves the stored image as a ImageIcon resized to a given width, maintaining
     * the original aspect ratio.
     * @param width
     * @return maintained aspect ratio, resized ImageIcon
     */
    public ImageIcon getImageIcon(int width) {
        ImageIcon og = getImageIcon();
        // cast one value to a float, otherwise int div occurs
        float ratio = (float)og.getIconWidth() / og.getIconHeight();
        int height = (int)(width / ratio);
        if (og.getIconWidth() == width && og.getIconHeight() == height) {
            return og;
        }
        return getImageIcon(width, height);
    }

    /**
     * Retrieves the stored image as a resized ImageIcon.
     * @param width
     * @param height
     * @return Resized ImageIcon to specified width and height param
     */
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

    /**
     * Check if another MyImage instance holds the same image.
     * @param other
     * @return true if no difference, false otherwise.
     */
    public boolean sameAs(MyImage other) {
        if (data.length != other.getData().length) { return false; }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != other.getData()[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resizes a MyImage instance (img) to a specified width maintaing its original
     * aspect ratio. Calculates appropriate height to do so.
     * @param img
     * @param width
     * @return resized MyImage instance, maintained aspect ratio
     */
    public static MyImage resize(MyImage img, int width)  {
        ImageIcon icon = img.getImageIcon(width);
        // BufferedImage bufImg = new BufferedImage(icon);
        return new MyImage(icon);
    }

    /**
     * Resizes a MyImage instance (img) to the specified width and height.
     * @param img
     * @param width
     * @param height
     * @return resized MyImage instance
     */
    public static MyImage resize(MyImage img, int width, int height) {
        ImageIcon icon = img.getImageIcon(width, height);
        // BufferedImage bufImg = new BufferedImage(icon);
        return new MyImage(icon);
    }
}
