import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Base {
    protected Socket socket;
    protected Message<?> lastMsg;

    protected ObjectInputStream objInStream;
    protected ObjectOutputStream objOutStream;

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
            lastMsg = (Message<?>)objInStream.readObject();
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

    /** Terminate/close the socket providing the connection to the other client/remote machine. */
    protected void closeConnection() {
        System.out.println("closeConnection called");
        sendMsg(new Message<String>(Message.Type.EXIT, ""));
        try {
            objInStream.reset();
            objInStream.close();
            objOutStream.flush();
            objOutStream.close();
            socket.close();
        } catch (IOException e) {}
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
}
