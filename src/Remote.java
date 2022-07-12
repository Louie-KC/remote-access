import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.time.Instant;
import java.time.Duration;

/**
* The class to have an instance created and run on the remote machine.
 */
public class Remote extends Base {
    private ServerSocket serverSocket;
    private Rectangle screenRect;
    private Robot robot;
    private MyImage lastSentScreen;
    private int clientReqWidth;
    private int clientReqHeight;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
            robot = new Robot();
            lastSentScreen = null;  // blank
            Thread.sleep(50);
        } catch (IOException | AWTException | InterruptedException e) {
            e.printStackTrace();
        }
        compareOS();

        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        // Set some value to start
        clientReqWidth = (int)screenRect.getWidth();
        clientReqHeight = (int)screenRect.getHeight();
    }

    @Override
    public void run() {
        while (true) {
            if (!receiveMsg()) {
                closeConnection();
                System.exit(0);
            }
            Instant actionStart = Instant.now();
            actionLastMsg();
            long duration = Duration.between(actionStart, Instant.now()).toMillis();
            System.out.println("--> Run loop: " + duration + "ms\n");
        }
    }

    @Override
    public void compareOS() {
        String thisOS = System.getProperty("os.name").toLowerCase();
        thisOS = thisOS.substring(0, 4);
        sendMsg(new Message<String>(Message.Type.INFO, thisOS));
    }

    /**
     * Checks last received message and invokes the appropriate method(s) to
     * process the message.
     */
    private void actionLastMsg() {
        switch (lastMsg.getType()) {
            case EXIT:
                System.out.println("Exit message received");
                try {
                    objInStream.close();
                    objOutStream.close();
                    socket.close();
                } catch (IOException e) {}
                System.exit(0);
                break;
            case IMG_REQUEST:
                sendScreen((String)lastMsg.getData());
                break;
            case KEY_PRESS:
                robot.keyPress((Integer)lastMsg.getData());
                break;
            case KEY_RELEASE:
                robot.keyRelease((Integer)lastMsg.getData());
                break;
            case MOUSE_CLICK:
                // fall through
            case MOUSE_RELEASE:
                // fall through
            case MOUSE_SCROLL:
                actionMouseMsg((MouseEvent) lastMsg.getData(),
                    lastMsg.getType() == Message.Type.MOUSE_CLICK);
                break;
            case FILE_INIT:
                beginFileReceiving();
            default:  // Do nothing
        }
    }

    /**
     * Captures the current (entire) screen, and sends it as a MyImage instance wrapped by the 
     * Message class via the ObjectOutputStream.
     */
    private void sendScreen(String msgData) {
        String[] data = msgData.split(" ");
        Instant begin = Instant.now();
        BufferedImage screenCap = robot.createScreenCapture(screenRect);
        long duration = Duration.between(begin, Instant.now()).toMillis();
        System.out.println("sendScreen cap: " + duration +"ms");
        if (data[0].isEmpty()) {
            sendScreenImg(new MyImage(screenCap));
            return;
        }
        clientReqWidth = Integer.valueOf(data[0]);
        if (data.length == 1) {
            sendScreenImg(new MyImage(MyImage.resizeToBytes(screenCap, clientReqWidth)));
            // Calculate height for mouse position adjustments
            float ratio = (float)(screenRect.getWidth() / clientReqWidth);
            clientReqHeight = (int)(screenRect.getHeight() / ratio);
            duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("sendScreen: " + duration +"ms");
            return;
        }
        clientReqHeight = Integer.valueOf(data[1]);
        if (data.length == 2) {
            sendScreenImg(new MyImage(MyImage.resizeToBytes(screenCap, clientReqWidth, clientReqHeight)));
            duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("sendScreen: " + duration + "ms");
        }
    }

    /**
     * Sends a MyImage instance if it is different to the last sent image. Otherwise
     * sends an IMG_NO_UPDATE message.
     * @param img
     */
    private void sendScreenImg(MyImage img) {
        if (img.sameAs(lastSentScreen)) {
            sendMsg(new Message<String>(Message.Type.IMG_NO_UPDATE, ""));
        } else {
            sendMsg(new Message<MyImage>(Message.Type.IMG_RESPONSE, img));
            lastSentScreen = img;
        }
    }

    /**
     * Corrects a mouse X position by a ratio describing the difference in size between the images
     * the Client receives and the actual capture size of the image before resizing and sending.
     * @param x mouse loc from Client side (to be corrected)
     * @param ratio the ratio between actual screen x size and client image x size
     */
    private int correctMouseX(int x, float ratio) {
        return (int)(x * ratio);
    }

     /**
      * Corrects a mouse Y position by a ratio describing the difference in size between the images
      * the Client receives and the actual capture size of the image before resizing and sending.
      * @param y mouse loc from Client side (to be corrected)
      * @param ratio the ratio between actual screen y size and client image y size
      */
    private int correctMouseY(int y, float ratio) {
        return (int)(y * ratio);
    }

    /**
     * Invokes the appropriate method for a received mouse input message on the robot instance.
     * Additionally finds the x and y ratios needed for mouse location correction before 
     * invoking the robot instances mousePress and mouseRelease methods.
     * @param e the MouseEvent (or MouseWheelEvent) from a Message
     * @param pressed Should the robot press or release, doesn't matter for MouseWheelEvent
     */
    private void actionMouseMsg(MouseEvent e, boolean pressed) {
        if (e instanceof MouseWheelEvent) {
            MouseWheelEvent wheelEvent = (MouseWheelEvent) e;
            if (wheelEvent.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                robot.mouseWheel(wheelEvent.getUnitsToScroll());
            }
            return;
        }
        float xRatio = (float)(screenRect.getWidth() / clientReqWidth);
        float yRatio = (float)(screenRect.getHeight() / clientReqHeight);
        robot.mouseMove(correctMouseX(e.getX(), xRatio), correctMouseY(e.getY(), yRatio));
        if (pressed) {
            robot.mousePress(MouseEvent.getMaskForButton(e.getButton()));
        } else {
            robot.mouseRelease(MouseEvent.getMaskForButton(e.getButton()));
        }
    }
}
