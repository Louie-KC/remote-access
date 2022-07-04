import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import javax.imageio.ImageIO;

/**
 * An extension to the java.awt.image.BufferedImage built in class, adding features
 * such as resizing and (de)serialisation for transfer via socket communication.
 */
public class MyImage implements Serializable {
    static final String FORMAT = "jpg";
    private byte[] data;
    private int knownWidth;
    private int knownHeight;

    public MyImage(byte[] imgData) {
        data = imgData;
        BufferedImage temp = getBufferedImage();
        knownWidth = temp.getWidth();
        knownHeight = temp.getHeight();
    }

    public MyImage(BufferedImage img) {
        data = MyImage.serialise(img);
        knownWidth = img.getWidth();
        knownHeight = img.getHeight();
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return knownWidth;
    }

    public int getHeight() {
        return knownHeight;
    }

    public BufferedImage getBufferedImage() {
        return MyImage.deserialise(data);
    }

    /**
     * Checks if the image contained is the same as another MyImage instance.
     * @param other
     * @return
     */
    public boolean sameAs(MyImage other) {
        if (other == null || data.length != other.getData().length) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != other.getData()[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resizes a BufferedImage to a specified width, maintaining its original aspect ratio.
     * @param img The image to be resized
     * @param width Resize width
     * @return Resized (original aspect ratio) BufferedImage
     */
    public static BufferedImage resize(BufferedImage img, int width) {
        float ratio = (float) img.getWidth() / img.getHeight();
        int height = (int) (width / ratio);
        return MyImage.resize(img, width, height);
    }

    /**
     * Resizes a BufferedImage to a specified width and height. To maintain the BufferedImages
     * aspect ratio use MyImage.resize(BufferedImage, int) instead.
     * @param img The image to be resized
     * @param width Resize width
     * @param height Resize height
     * @return Resized BufferedImage
     */
    public static BufferedImage resize(BufferedImage img, int width, int height) {
        double xScale = (double) width / img.getWidth();
        double yScale = (double) height / img.getHeight();
        AffineTransform transform = AffineTransform.getScaleInstance(xScale, yScale);
        AffineTransformOp scaler = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return scaler.filter(img, new BufferedImage(width, height, img.getType()));
    }

    /**
     * Resizes to a specified width, maintaing the original aspect ratio.
     * @param width Resize width
     * @return resized image (maintain aspect ratio)
     */
    public byte[] asSize(int width) {
        return MyImage.resizeToBytes(deserialise(getData()), width);
    }

    /**
     * Resizes the image to the specified width and height.
     * @param width Resize width
     * @param height Resize height
     * @return resized image
     */
    public byte[] asSize(int width, int height) {
        return MyImage.resizeToBytes(deserialise(getData()), width, height);
    }

    /**
     * Serialise a BufferedImage. Formats the image and returns its byte data for
     * transmission.
     * Brought back to a BufferedImage with the MyImage.deserialise(byte[]) method.
     * @param img the Image to serialise
     * @return Image byte array
     */
    private static byte[] serialise(BufferedImage img) {
        Instant begin = Instant.now();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, FORMAT, baos);
            long duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("serialise: " + duration + "ms");
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Converts the serialised byte data of a BufferedImage back to a BufferedImage
     * instance, which is then returned.
     * @param imgData the byte buffer to extract a BufferedImage from
     * @return BufferedImage from byte array
     */
    private static BufferedImage deserialise(byte[] imgData) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imgData));
        } catch (Exception e) {
            System.err.println("MyImage deserialise exception");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize and serialise a BufferedImage to a specified width, maintaining its aspect ratio.
     * @param img Image to be resized
     * @param width Resize width
     * @return Serialised byte data for the resized image.
     */
    public static byte[] resizeToBytes(BufferedImage img, int width) {
        BufferedImage resized = MyImage.resize(img, width);
        return MyImage.serialise(resized);
    }

    /**
     * Resize and serialise a BufferedImage to a specified size.
     * @param img The image to be resized
     * @param width Resize width
     * @param height Resize height
     * @return Serialised byte data for the resized image.
     */
    public static byte[] resizeToBytes(BufferedImage img, int width, int height) {
        BufferedImage resized = MyImage.resize(img, width, height);
        return MyImage.serialise(resized);
    }
}