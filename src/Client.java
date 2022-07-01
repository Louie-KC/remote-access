import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.time.Instant;
import java.time.Duration;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class Client extends Base {
    InputReader inputReader;
    JFrame frame;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    
    public Client(String targetIP, int targetPort) {
        inputReader = new InputReader(this);
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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);

        frame.addKeyListener(inputReader);
        frame.addMouseListener(inputReader);
        frame.addWindowListener(new WindowAdapter() {  // Frame/window closing procedure
            @Override
            public void windowClosing(WindowEvent event) {
                sendMsg(new Message<String>(Message.Type.EXIT, ""));
                System.out.println("Exit message sent");
                frame.dispose();
                System.exit(0);
            }
        });

        System.out.println("Sleeping 1s");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.getMessage();
        }
        JLabel screenImageLabel = new JLabel();

        while (true) {
            Instant loopStart = Instant.now();
            sendMsg(new Message<String>(Message.Type.IMG_REQUEST, WIDTH + " " + HEIGHT));
            if (!receiveMsg()) { System.exit(0); }
            ImageIcon receivedScreenIcon = readScreenImg();
            if (receivedScreenIcon != null) {  // If an update was received
                screenImageLabel.setIcon(receivedScreenIcon);
                frame.add(screenImageLabel);
                frame.pack();
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
     * Checks for and reads an ImageIcon from the last received message.
     * @return ImageIcon if present, null otherwise.
     */
    private ImageIcon readScreenImg() {
        if (lastMsg != null && lastMsg.getType().equals(Message.Type.IMG_RESPONSE)) {
            if (lastMsg.getData() instanceof MyImage) {
                return ((MyImage)lastMsg.getData()).getImageIcon(WIDTH, HEIGHT);
            }
        }
        if (lastMsg.getType().equals(Message.Type.IMG_NO_UPDATE)) {
            System.out.println("Screen no update msg received");
            return null;
        }
        System.out.println("readScreenImg: invalid msg");
        return null;
    }
}
