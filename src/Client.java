import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.time.Duration;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Client extends Base {
    private Window window;
    private int requestWidth;
    private int requestHeight;
    private int framePanelWidthDiff;
    private boolean remoteOnSameOS;
    
    public Client(String targetIP, int targetPort) {
        // Establish connection
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Init
        compareOS();
        requestWidth = 800;
        requestHeight = 600;
        window = new Window(this, new InputReader(this));
        framePanelWidthDiff = window.getWidth() - window.getPanel().getWidth();

        // Setting custom frame operations/procedures
        window.setDefaultCloseOperation(0);  // 0 = JFrame.DO_NOTHING_ON_CLOSE
        window.addWindowListener(new WindowAdapter() {  // Procedure on frame/window close
            @Override
            public void windowClosing(WindowEvent event) {
                sendMsg(new Message<String>(Message.Type.EXIT, ""));
                System.out.println("Exit message sent");
                window.dispose();
                System.exit(0);
            }
        });
        window.addComponentListener(new ComponentAdapter() {  // Procedure on frame resize
            @Override
            public void componentResized(ComponentEvent e) {
                setRequestImgSize(window.getWidth() - framePanelWidthDiff, window.getHeight());
                window.resizeWindow();
            }
        });
    }

    @Override
    public void run() {
        while (true) {
            Instant loopStart = Instant.now();
            sendMsg(new Message<String>(Message.Type.IMG_REQUEST, getRequestImgWidth() + ""));
            if (!receiveMsg()) { System.exit(0); }
            MyImage receivedScreenImg = readScreenImg();
            if (receivedScreenImg != null) {  // If an update was received, set img and repaint
                window.setScreenImage(receivedScreenImg);
                window.repaint();
            }
            Instant loopEnd = Instant.now();
            try {
                // Sleep until 1/3 of a second has passed since the above loop began
                Thread.sleep(333L - Duration.between(loopStart, loopEnd).toMillis());
            } catch (InterruptedException | IllegalArgumentException e) {
                e.getMessage();
            }
        }
    }

    @Override
    public void compareOS() {
        String thisOS = System.getProperty("os.name").toLowerCase();
        thisOS = thisOS.substring(0, 4);
        if (!receiveMsg()) { System.exit(1); }
        if (lastMsg.getType().equals(Message.Type.INFO)) {
            if (lastMsg.getData().equals(thisOS)) {
                remoteOnSameOS = true;
            } else {
                remoteOnSameOS = false;
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

    /** 
     * Returns whether the client has discovered the remote machines OS to be 
     * the same. Value is set by the compareOS method.
     */
    public boolean getRemoteOnSameOS() {
        return remoteOnSameOS;
    }

    public int getRequestImgWidth() { return requestWidth; }

    public int getRequestImgHeight() { return requestHeight; }

    /** Updates the image size which the client requests from the remote machine */
    public void setRequestImgSize(int newWidth, int newHeight) {
        requestWidth = newWidth;
        requestHeight = newHeight;
    }
}
