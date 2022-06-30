import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.image.BufferedImage;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

public class Remote extends Base {
    ServerSocket serverSocket;
    Rectangle screenRect;
    Robot robot;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
            robot = new Robot();
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
        if (lastMsg.getType().equals(Message.Type.IMG_REQUEST)) {
            sendScreenImg();
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
        BufferedImage img = robot.createScreenCapture(screenRect);
        sendMsg(new Message<MyImage>(Message.Type.IMG_RESPONSE, new MyImage(img)));
        System.out.println("Screen capture message sent");
    }

}
