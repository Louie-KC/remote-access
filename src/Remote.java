import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Point;
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

    private float xMouseCorrection;
    private float yMouseCorrection;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            socket.setSoTimeout(4000);
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
        targetWidth = (int) screenRect.getWidth();
        targetHeight = (int) screenRect.getHeight();
        xMouseCorrection = 1f;
        yMouseCorrection = 1f;

        // Remote side window test
        window = new Window(this, "Remote Access - Remote");
        setTerminateOnWindowClose();
        window.addMenuButton(Window.EXIT_BUTTON_TEXT);
        window.setSize(480, 60);
        window.setAlwaysOnTop(true);
        window.setResizable(false);
    }

    @Override
    public void run() {
        while (isConnected()) {
            if (!receiveMsg()) {
                closeConnection();
                break;
            }
            Instant actionStart = Instant.now();
            actionLastMsg();
            long duration = Duration.between(actionStart, Instant.now()).toMillis();
            System.out.println("--> Run loop: " + duration + "ms\n");
        }
        System.exit(0);
    }

    @Override
    public void compareOS() {
        String thisOS = System.getProperty("os.name").toLowerCase();
        thisOS = thisOS.substring(0, 4);
        sendMsg(new Message<String>(Message.Type.INFO, thisOS));
    }

    /* Uses the Remotes Robot instance to paste the systems clipboard contents. */
    void pasteClipboardText() {
        int ctrlOrCMD = KeyEvent.VK_CONTROL;
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            ctrlOrCMD = KeyEvent.VK_META;
        }
        robot.keyPress(ctrlOrCMD);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(5);  // Short delay to give OS time to paste
        robot.keyRelease(ctrlOrCMD);
        robot.keyRelease(KeyEvent.VK_V);
    }

    /**
     * Extends the base actionLastMsg method, handling image requests and remote
     * user input messages.
     */
    @Override
    void actionLastMsg() {
        super.actionLastMsg();
        switch (lastMsg.getType()) {
            case IMG_REQUEST:
                sendScreen((String) lastMsg.getData());
                break;
            case KEY_PRESS:
                robot.keyPress((Integer) lastMsg.getData());
                break;
            case KEY_RELEASE:
                robot.keyRelease((Integer) lastMsg.getData());
                break;
            case MOUSE_POS:
                Point p = (Point) lastMsg.getData();
                robot.mouseMove(correctMouseX(p.x), correctMouseY(p.y));
                break;
            case MOUSE_CLICK:
                // fall through
            case MOUSE_RELEASE:
                // fall through
            case MOUSE_SCROLL:
                actionMouseMsg((MouseEvent) lastMsg.getData(),
                    lastMsg.getType() == Message.Type.MOUSE_CLICK);
                break;
            case CLIPBOARD_DATA:
                pasteClipboardText();
                break;
            case CLIPBOARD_REQUEST:
                sendClipboardText();
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
        targetWidth = Integer.valueOf(data[0]);
        if (data.length == 1) {
            sendScreenImg(new MyImage(MyImage.resizeToBytes(screenCap, targetWidth)));
            // Calculate height for mouse position adjustments
            float ratio = (float)(screenRect.getWidth() / targetWidth);
            targetHeight = (int)(screenRect.getHeight() / ratio);
            updateMouseCorrection(targetWidth, targetHeight);
            duration = Duration.between(begin, Instant.now()).toMillis();
            System.out.println("sendScreen: " + duration +"ms");
            return;
        }
        targetHeight = Integer.valueOf(data[1]);
        if (data.length == 2) {
            sendScreenImg(new MyImage(MyImage.resizeToBytes(screenCap, targetWidth, targetHeight)));
            updateMouseCorrection(targetWidth, targetHeight);
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
     * Corrects a given mouse x position by the last knwon x mouse correction ratio.
     * @param x mouse loc from Client side (to be corrected)
     */
    private int correctMouseX(int x) {
        return (int) (x * xMouseCorrection);
    }

     /**
      * Corrects a given mouse y position by the last known y mouse correction ratio.
      * @param y mouse loc from Client side (to be corrected)
      */
    private int correctMouseY(int y) {
        return (int) (y * yMouseCorrection);
    }


    private void updateMouseCorrection(int width, int height) {
        xMouseCorrection = (float) (screenRect.getWidth() / targetWidth);
        yMouseCorrection = (float) (screenRect.getHeight() / targetHeight);
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
        robot.mouseMove(correctMouseX(e.getX()), correctMouseY(e.getY()));
        if (pressed) {
            robot.mousePress(MouseEvent.getMaskForButton(e.getButton()));
        } else {
            robot.mouseRelease(MouseEvent.getMaskForButton(e.getButton()));
        }
    }
}
