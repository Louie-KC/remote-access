import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.image.BufferedImage;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.Toolkit;

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
     * Checks the last received message and invokes the appropriate method to process
     * the message.
     */
    private void actionLastMsg() {
        String type = lastMsg.getType();
        if (type.equals("imgreq")) {
            sendScreenImg();
            return;
        }
    }

    /**
     * Captures the current (entire) screen, and sends as ImageIcon in a Message
     * via the ObjectOutputStream.
     */
    private void sendScreenImg() {
        BufferedImage img = robot.createScreenCapture(screenRect);
        sendMsg(new Message<MyImage>("img", new MyImage(img)));
        System.out.println("Screen capture message sent");
    }

}
