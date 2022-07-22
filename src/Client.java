import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import java.time.Duration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The class to have an instance created and run on the client/terminal machine.
 */
public class Client extends Base {
    private int framePanelWidthDiff;
    private boolean remoteOnSameOS;
    private boolean sizeChanged;
    
    public Client(String targetIP, int targetPort) {
        // Establish connection
        try {
            socket = new Socket(targetIP, targetPort);
            socket.setSoTimeout(4000);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
            Thread.sleep(50);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Init
        compareOS();
        targetWidth = 800;
        targetHeight = 600;
        window = new Window(this, "Remote Access - Client", new InputReader(this));
        framePanelWidthDiff = window.getWidth() - window.getPanel().getWidth();

        // Setting custom frame operations/procedures
        setTerminateOnWindowClose();
        window.addComponentListener(new ComponentAdapter() {  // Procedure on frame resize
            @Override
            public void componentResized(ComponentEvent e) {
                setRequestImgSize(window.getWidth() - framePanelWidthDiff, window.getHeight());
                window.resizeWindow();
            }
        });
        // Create window menu items
        window.addMenuButton(Window.SEND_FILE);
        window.addMenuButton(Window.RECEIVE_FILE);
        window.addMenuButton(Window.SEND_CLIPBOARD);
        window.addMenuButton(Window.GET_CLIPBOARD);
        window.addMenuButton(Window.EXIT_BUTTON_TEXT);
    }

    @Override
    public void run() {
        while (isConnected()) {
            Instant loopStart = Instant.now();
            requestScreenImg(true);
            if (!receiveMsg()) {
                closeConnection();
                break;
            }
            actionLastMsg();
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
        System.exit(0);
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

    @Override
    void actionLastMsg() {
        super.actionLastMsg();
        if (lastMsg.getType().equals(Message.Type.CLIPBOARD_DATA)) {
            setClipboardWithMsgText();
        }
    }

    /**
     * Sends an IMG_REQUEST message to the remote machine. If the Window size has been
     * recorded to have changed, adds image size details to the message for the remote
     * machine to resize to.
     * @param maintainAspectRatio send height/y value, or let remote decide appropriate value
     */
    private void requestScreenImg(boolean maintainAspectRatio) {
        String reqString = "";
        if (sizeChanged) {
            System.out.println("sizeChanged was true");
            reqString = "x" + getRequestImgWidth();
            if (!maintainAspectRatio) {
                reqString += "y" + getRequestImgHeight();
            }
            sizeChanged = false;
        }
        System.out.println("Requesting size: " + reqString);
        sendMsg(new Message<String>(Message.Type.IMG_REQUEST, reqString));
    }

    /**
     * Checks last message for a MyImage instance and returns it.
     * @return MyImage if present, null otherwise.
     */
    private MyImage readScreenImg() {
        if (lastMsg != null && lastMsg.getType().equals(Message.Type.IMG_RESPONSE)) {
            if (lastMsg.getData() instanceof MyImage) {
                MyImage rcvdImg = (MyImage) lastMsg.getData();
                if (rcvdImg.getWidth() == getRequestImgWidth()) { sizeChanged = false; }
                // return ((MyImage) lastMsg.getData());
                return rcvdImg;
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

    public int getRequestImgWidth() { return targetWidth; }

    public int getRequestImgHeight() { return targetHeight; }

    /** Updates the image size which the client requests from the remote machine */
    public void setRequestImgSize(int newWidth, int newHeight) {
        targetWidth = newWidth;
        targetHeight = newHeight;
        sizeChanged = true;
    }
}
