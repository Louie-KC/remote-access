import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Base {
    
    public Client(String targetIP, int targetPort) {
        try {
            socket = new Socket(targetIP, targetPort);
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            objInStream = new ObjectInputStream(socket.getInputStream());
            // socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Messaging test
        sendMsg(new Message<String>("text", "to remote test"));
        Message rcvd = receiveMsg();
        if (rcvd.getType().equals("text")) {
            System.out.println((String)rcvd.getData());
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
