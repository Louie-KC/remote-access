import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.time.Duration;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class Client extends Base {
    Window displayWindow;
    final int WIDTH = 800;
    final int HEIGHT = 600;
    
    public Client(String targetIP, int targetPort) {
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        displayWindow = new Window(this, new InputReader(this));
        displayWindow.setDefaultCloseOperation(0);  // 0 = JFrame.DO_NOTHING_ON_CLOSE
        displayWindow.addWindowListener(new WindowAdapter() {  // Set window close operation
            @Override
            public void windowClosing(WindowEvent event) {
                sendMsg(new Message<String>(Message.Type.EXIT, ""));
                System.out.println("Exit message sent");
                System.exit(0);
            }
        });
    }

    @Override
    public void run() {
        System.out.println("Sleeping 1s");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getMessage();
        }

        while (true) {
            Instant loopStart = Instant.now();
            sendMsg(new Message<String>(Message.Type.IMG_REQUEST, WIDTH + ""));
            if (!receiveMsg()) { System.exit(0); }
            MyImage receivedScreenImg = readScreenImg();
            if (receivedScreenImg != null) {  // If an update was received, set img and repaint
                displayWindow.setScreenImage(receivedScreenImg);
                displayWindow.repaint();
            }
            Instant loopEnd = Instant.now();
            try {
                // Sleep for 1/3 of a second
                Thread.sleep(333L - Duration.between(loopStart, loopEnd).toMillis());
            } catch (InterruptedException | IllegalArgumentException e) {
                e.getMessage();
            }
        }
    }

    /**
     * Checks last message for a MyImage instance and returns it.
     * @return MyImage if present, null otherwise.
     */
    private MyImage readScreenImg() {
        if (lastMsg != null && lastMsg.getType().equals(Message.Type.IMG_RESPONSE)) {
            if (lastMsg.getData() instanceof MyImage) {
                // return ((MyImage)lastMsg.getData()).getBufferedImage();
                return ((MyImage)lastMsg.getData());
            }
        }
        return null;
    }
}
