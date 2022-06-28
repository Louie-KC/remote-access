import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Base {
    Socket socket;
    Message<?> lastMsg;

    ObjectInputStream objInStream;
    ObjectOutputStream objOutStream;

    /**
     * Sends a Message (serialisable) object via the programs ObjectOutputStream.
     * @param msg
     * @return true if sucessful, false otherwise
     */
    public boolean sendMsg(Message<?> msg) {
        try {
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
}
