import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Base {
    Socket socket;
    Message<?> lastMsg;

    ObjectInputStream objInStream;
    ObjectOutputStream objOutStream;

    Window window;

    int targetWidth;
    int targetHeight;

    public abstract void run();

    public abstract void compareOS();

    /**
     * Sends a Message (serialisable) object via the programs ObjectOutputStream.
     * @param msg
     * @return true if sucessful, false otherwise
     */
    public boolean sendMsg(Message<?> msg) {
        try {
            if (!isConnected()) { return false; }
            objOutStream.writeObject(msg);
            System.out.println("Message sent type: " + msg.getType());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads a Message object from the ObjectInputStream into lastMsg variable.
     * @return true is sucessful, false otherwise
     */
    public boolean receiveMsg() {
        try {
            if (!isConnected()) { return false; }
            lastMsg = (Message<?>) objInStream.readObject();
            return true;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the socket is connected AND is open.
     * @return Socket connected and open status.
     */
    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    /** Closes all streams then the Socket instance */
    private void closeStreamsAndSocket() {
        System.out.println("Closing connection socket and streams");
        try {
            if (objInStream != null) { objInStream.close(); }
            if (objOutStream != null) {
                objOutStream.flush();
                objOutStream.close();
            }
            socket.close();
        } catch (IOException e) {}  // do nothing
    }

    /** 
     * Checks if the last received message is the EXIT message, invoking closeStreamsAndSocket
     * and exits the program if EXIT.
     */
    void checkForExitMsg() {
        if (lastMsg.getType().equals(Message.Type.EXIT)) {
            closeStreamsAndSocket();
            System.exit(0);
        }
    }

    /** Terminate/close the socket providing the connection to the other client/remote machine. */
    void closeConnection() {
        System.out.println("closeConnection called");
        sendMsg(new Message<String>(Message.Type.EXIT, ""));
        closeStreamsAndSocket();
        System.exit(0);
    }

    /** Start file sending process */
    void beginFileSending() {
        int newPort = socket.getPort() + 1;
        sendMsg(new Message<Integer>(Message.Type.FILE_INIT, newPort));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new Thread(new FileSender(socket.getInetAddress().getHostAddress(),
            newPort, frame)).start();
    }

    /** Start the file receiving process */
    void beginFileReceiving() {
        new Thread(new FileReceiver((Integer) lastMsg.getData())).start();
    }

    /**
     * Sets the JFrame windows closing operation to dispose the window, invoke the Base classes
     * closeConnection method and stop program execution.
     * 
     * Must be called after the JFrame window instance has been initialised.
     */
    void setTerminateOnWindowClose() {
        if (window == null) {
            System.out.println("setWindowCloseOperation called too early, window == null");
            return;
        }
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                window.dispose();
                closeConnection();
                System.exit(0);
            }
        });
    }
}
