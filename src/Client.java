import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Client extends Base {
    JFrame frame;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    
    public Client(String targetIP, int targetPort) {
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("Client test");
        frame.setSize(WIDTH,HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Testing if reading locks
        System.out.println("Sleeping 3s");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.getMessage();
        }
        sendMsg(new Message<String>("imgreq", ""));
        if (!receiveMsg()) { System.exit(0); }
        System.out.println("Received  message");

        JLabel imgTest = new JLabel();
        imgTest.setIcon(readScreenImg());
        frame.add(imgTest);
        frame.pack();
    }

    /**
     * Checks for and reads an ImageIcon from the last received message.
     * @return ImageIcon if present, null otherwise.
     */
    private ImageIcon readScreenImg() {
        if (lastMsg != null && lastMsg.getType().equals("img")) {
            if (lastMsg.getData() instanceof MyImage) {
                return ((MyImage)lastMsg.getData()).getImageIcon(WIDTH, HEIGHT);
            }
        }
        System.out.println("readScreenImg: invalid msg");
        return null;
    }
}
