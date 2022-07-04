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
import java.time.Instant;
import java.time.Duration;

public class Remote extends Base {
    ServerSocket serverSocket;
    Rectangle screenRect;
    Robot robot;
    MyImage lastSentScreen;
    int clientReqWidth;
    int clientReqHeight;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
            robot = new Robot();
            lastSentScreen = null;  // blank
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        // Set some value to start
        clientReqWidth = (int)screenRect.getWidth();
        clientReqHeight = (int)screenRect.getHeight();
    }

    @Override
    public void run() {
        while (true) {
            if (!receiveMsg()) { System.exit(0); }
            Instant actionStart = Instant.now();
            actionLastMsg();
            long duration = Duration.between(actionStart, Instant.now()).toMillis();
            System.out.println("--> Run loop: " + duration + "ms\n");
        }
    }

    /**
     * Checks last received message and invokes the appropriate method(s) to
     * process the message.
     */
    private void actionLastMsg() {
        if (lastMsg.getType().equals(Message.Type.EXIT)) {
            System.out.println("Exit message received");
            System.exit(0);
        }
        if (lastMsg.getType().equals(Message.Type.IMG_REQUEST)) {
            sendScreen((String)lastMsg.getData());
        }
        if (lastMsg.getType().equals(Message.Type.KEY_PRESS)) {
            robot.keyPress((Integer)lastMsg.getData());
        }
        if (lastMsg.getType().equals(Message.Type.KEY_RELEASE)) {
            robot.keyRelease((Integer)lastMsg.getData());
        }
        if (lastMsg.getType().equals(Message.Type.MOUSE_CLICK)) {
            actionMouseMsg((MouseEvent)lastMsg.getData(), true);
        }
        if (lastMsg.getType().equals(Message.Type.MOUSE_RELEASE)) {
            actionMouseMsg((MouseEvent)lastMsg.getData(), false);
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

    private int correctMouseX(int x, float ratio) {
        return (int)(x * ratio);
    }

    private int correctMouseY(int y, float ratio) {
        return (int)(y * ratio);
    }

    private void actionMouseMsg(MouseEvent e, boolean pressed) {
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
