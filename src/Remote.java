import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

public class Remote extends Base {
    ServerSocket serverSocket;

    public Remote(int targetPort) {
        try {
            serverSocket = new ServerSocket(targetPort);
            socket = serverSocket.accept();
            objInStream = new ObjectInputStream(socket.getInputStream());
            objOutStream = new ObjectOutputStream(socket.getOutputStream());
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

        // testing
        Message rcvd = receiveMsg();
        if (rcvd.getType().equals("text")) {
            System.out.println("received: " + (String)rcvd.getData());
        }
        sendMsg(new Message<String>("text", "response from remote"));

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
