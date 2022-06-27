import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Base {
    Socket socket;

    ObjectInputStream objInStream;
    ObjectOutputStream objOutStream;

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

    public Message<?> receiveMsg() {
        try {
            return (Message<?>)objInStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
