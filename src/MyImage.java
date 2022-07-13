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
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

// local testing
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Rectangle;
import javax.swing.JFrame;

/**
 * An extension to the java.awt.image.BufferedImage built in class, adding features
 * such as resizing and (de)serialisation for transfer via socket communication.
 */
public class MyImage implements Serializable {
    static final String FORMAT = "JPG";
    private byte[] data;
    private int knownWidth;
    private int knownHeight;

    private static boolean log = false;

    public MyImage(byte[] imgData) {
        data = imgData;
        BufferedImage temp = getBufferedImage();
        knownWidth = temp.getWidth();
        knownHeight = temp.getHeight();
    }

    public MyImage(BufferedImage img) {
        // data = MyImage.serialise(img);
        data = MyImage.jpgFullSerialise(img);
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
        return MyImage.progressiveResize(img, width, height);
        // return MyImage.directResize(img, width, height);
    }

    /**
     * Resizes a BufferedImage to a specified width and height. To maintain the BufferedImages
     * aspect ratio use MyImage.resize(BufferedImage, int) instead.
     * @param img The image to be resized
     * @param width Resize width
     * @param height Resize height
     * @return Resized BufferedImage
     */
    public static BufferedImage directResize(BufferedImage img, int width, int height) {
        Instant begin = Instant.now();

        double xScale = (double) width / img.getWidth();
        double yScale = (double) height / img.getHeight();
        AffineTransform transform = AffineTransform.getScaleInstance(xScale, yScale);
        AffineTransformOp scaler = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage ret = scaler.filter(img, new BufferedImage(width, height, img.getType()));
        if (log) {
            long duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("directResize: " + duration + "ms");
        }
        return ret;
    }

    /**
     * Repeatedly halves the image size while the target size is less than half of the images
     * current size, then resizes to the specified size. Done to preserve detail in the image.
     * @param img The image to be resized
     * @param width Resize width
     * @param height Resize height
     * @return Resized BufferedImage
     */
    public static BufferedImage progressiveResize(BufferedImage img, int width, int height) {
        Instant begin = Instant.now();
        
        double xScale = (double) width / img.getWidth();
        double yScale = (double) height / img.getHeight();
        while (xScale < 0.5f && yScale < 0.5f) {
            img = MyImage.directResize(img, img.getWidth()/2, img.getHeight()/2);
            xScale = (double) width / img.getWidth();
            yScale = (double) height / img.getHeight();
        }
        img = MyImage.directResize(img, width, height);
        if (log) {
            long duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("progressiveResize: " + duration + "ms");
        }
        return img;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, FORMAT, baos);
            if (log) {
                long duration = Duration.between(begin, Instant.now()).toMillis();
                System.out.println("serialise: " + duration + "ms");
                System.out.println("serialise size (bytes): " + baos.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     * Serialise a BufferedImage to JPG format at fully quality compression. Returns the
     * byte data for the resulting JPG, to be brought back to a MyImage with the
     * MyImage.deserialise(byte[]) method.
     * @param img the Image to be serialised
     * @return Image byte array
     */
    private static byte[] jpgFullSerialise(BufferedImage img) {
        if (!FORMAT.equals("JPG")) { return new byte[0]; }
        Instant begin = Instant.now();

        JPEGImageWriteParam param = new JPEGImageWriteParam(null);
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1f);

        ImageWriter writer = ImageIO.getImageWritersByFormatName(FORMAT).next();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(baos);
            writer.setOutput(imgOutStream);
            writer.write(img);
        } catch (IOException e) { 
            System.out.println("Error serialising");
        }
        if (log) {
            long duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("jpgFullSerialise: " + duration + "ms");
            System.out.println("jpgFullSerialise size (bytes): " + baos.size());
        };
        return baos.toByteArray();
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
        // return MyImage.serialise(resized);
        return MyImage.jpgFullSerialise(resized);
    }

    /**
     * Resize and serialise a BufferedImage to a specified size.
     * @param img The image to be resized
     * @param width Resize width
     * @param height Resize height
     * @return Serialised byte data for the resized image.
     */
    public static byte[] resizeToBytes(BufferedImage img, int width, int height) {
        BufferedImage resized = MyImage.directResize(img, width, height);
        // return MyImage.serialise(resized);
        return MyImage.jpgFullSerialise(resized);
    }

    // Capture, resize, serialise and deserialise performance test
    public static void main(String[] args) {
        log = true;
        int targetWidth = 800;
        int testRuns = 100;
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(targetWidth, 600);
            frame.setVisible(true);
            BufferedImage resizeTest = null;

            Instant begin = Instant.now();
            for (int i = 0; i < testRuns; i++) {
                resizeTest = MyImage.resize(robot.createScreenCapture(screenRect), targetWidth);
                // resizeTest = MyImage.deserialise(MyImage.jpgFullSerialise(resizeTest));
                resizeTest = MyImage.deserialise(MyImage.serialise(resizeTest));
                System.out.println();
            }
            long duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("Total duration: " + duration +
                "\nAverage duration: " + duration/testRuns);
            frame.getGraphics().drawImage(resizeTest, 0, 0, null);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}