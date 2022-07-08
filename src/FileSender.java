import java.io.File;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileSender implements Runnable {
    private final int FOUR_KB = 1024*4;
    private Socket socket;
    private DataOutputStream dataOutStream;
    private File file;

    public FileSender(String targetIP, int targetPort, File fileToSend) {
        try {
            System.out.println("FileSender: Making connection");
            socket = new Socket(targetIP, targetPort);
            dataOutStream = new DataOutputStream(socket.getOutputStream());
            file = fileToSend;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sends a file in 4 KB chunks */
    public void sendFile() throws Exception {
        System.out.println("FileSender: Start sending file");
        FileInputStream fileInStream = new FileInputStream(file);
        dataOutStream.writeLong(file.length());
        byte[] buffer = new byte[FOUR_KB];     
        for (int wBytes = fileInStream.read(buffer); wBytes != -1; wBytes = fileInStream.read(buffer)) {
            dataOutStream.write(buffer, 0, wBytes);
            dataOutStream.flush();
        }
        fileInStream.close();
        System.out.println("FileSender: Sending complete");
    }

    @Override
    public void run() {
        try {
            if (file != null && file.exists()) {
                sendFile();
            }
        } catch (Exception e) {
            System.err.println("Error sending file");
        } finally {
            try {
                dataOutStream.flush();
                dataOutStream.close();
                socket.close();
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) {
        File testFile = new File("./.gitignore");
        FileSender test = new FileSender("127.0.0.1", 7779, testFile);
        new Thread(test).run();
    }
    
}
