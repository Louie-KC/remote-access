import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.*;

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

        // testing
        sendScreenImg();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Captures the current (entire) screen, and sends as ImageIcon in a Message
     * via the ObjectOutputStream.
     */
    private void sendScreenImg() {
        BufferedImage img = robot.createScreenCapture(screenRect);
        sendMsg(new Message<ImageIcon>("img", new ImageIcon(img)));
        System.out.println("Screen capture message sent");
    }
}
