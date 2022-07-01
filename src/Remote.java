import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

public class Remote extends Base {
    ServerSocket serverSocket;
    Rectangle screenRect;
    Robot robot;
    MyImage curScreen;
    MyImage lastScreen;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
            robot = new Robot();
            lastScreen = new MyImage(new byte[] {});  // blank
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
            String dimensions[] = ((String)lastMsg.getData()).split(" ");
            sendScreenImg(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]));
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
    private void sendScreenImg() {
        MyImage img = new MyImage(robot.createScreenCapture(screenRect));
        sendMsg(new Message<MyImage>(Message.Type.IMG_RESPONSE, img));
        System.out.println("Screen capture message sent");
    }

    /**
     * Captures the current (entire) screen, resizing to the specified width and height.
     * Creates a new Message containing the resized screencapture, and sends via sendMsg method.
     * @param width resize width
     * @param height resize height
     */
    private void sendScreenImg(int width, int height) {
        MyImage img = new MyImage(robot.createScreenCapture(screenRect));
        MyImage resized = MyImage.resize(img, width, height);
        if (resized.sameAs(lastScreen)) {
            sendMsg(new Message<String>(Message.Type.IMG_NO_UPDATE, ""));
        } else {
            sendMsg(new Message<MyImage>(Message.Type.IMG_RESPONSE, resized));
        }
        lastScreen = resized;
    }

}
