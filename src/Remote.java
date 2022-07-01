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

public class Remote extends Base {
    ServerSocket serverSocket;
    Rectangle screenRect;
    Robot robot;
    MyImage curScreen;
    MyImage lastSentScreen;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
            robot = new Robot();
            lastSentScreen = new MyImage(new byte[] {});  // blank
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }

    @Override
    public void run() {
        while (true) {
            if (!receiveMsg()) { System.exit(0); }
            actionLastMsg();
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
            MouseEvent mEvent = (MouseEvent)lastMsg.getData();
            robot.mouseMove(mEvent.getX(), mEvent.getY());
            robot.mousePress(MouseEvent.getMaskForButton(mEvent.getButton()));
        }
        if (lastMsg.getType().equals(Message.Type.MOUSE_RELEASE)) {
            MouseEvent mEvent = (MouseEvent)lastMsg.getData();
            robot.mouseMove(mEvent.getX(), mEvent.getY());
            robot.mouseRelease(MouseEvent.getMaskForButton(mEvent.getButton()));
        }
    }

    /**
     * Captures the current (entire) screen, and sends as ImageIcon in a Message
     * via the ObjectOutputStream.
     */
    private void sendScreen(String msgData) {
        String[] data = msgData.split(" ");
        BufferedImage screenCap = robot.createScreenCapture(screenRect);
        if (data[0].isEmpty()) {
            sendScreenImg(new MyImage(screenCap));
        } else if (data.length == 1) {
            sendScreenImg(MyImage.resize(new MyImage(screenCap), Integer.valueOf(data[0])));
        } else if (data.length == 2) {
            sendScreenImg(MyImage.resize(new MyImage(screenCap),
                            Integer.valueOf(data[0]), Integer.valueOf(data[1])));
        }
    }

    /**
     * Wraps and sends a MyImage (screen) image if the screen has changed since
     * the last sent image
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

}
