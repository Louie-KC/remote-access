import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver implements Runnable {
    private final int FOUR_KB = 1024*4;
    private Socket socket;
    private DataInputStream dataInStream;
    private FileOutputStream fileOutStream;
    private String fileWriteDir;

    public FileReceiver(int port, String writeLoc) {
        try {
            fileWriteDir = writeLoc;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("FileReceiver: waiting for connection");
            socket = serverSocket.accept();
            serverSocket.close();
            dataInStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Reads a file in 4KB chunks and writes to disk. */
    public void receiveFile() throws Exception {
        System.out.println("FileReceiver: started download");
        int rBytes = 0;
        
        fileOutStream = new FileOutputStream(fileWriteDir);
        long size = dataInStream.readLong();
        byte[] buffer = new byte[FOUR_KB];
        rBytes = dataInStream.read(buffer, 0, (int) Math.min(buffer.length, size));
        while (size > 0 && rBytes != -1) {
            fileOutStream.write(buffer, 0, rBytes);
            size -= rBytes;
            rBytes = dataInStream.read(buffer, 0, (int) Math.min(buffer.length, size));
        }
        System.out.println("Finished downloading file");
    }

    @Override
    public void run() {
        try {
            receiveFile();
        } catch (Exception e) {
            System.err.println("Error downloading file");
            e.printStackTrace();
        } finally {
            try {
                fileOutStream.close();
                dataInStream.close();
                socket.close();
            } catch (IOException e) {}
        }   
    }

    public static void main(String[] args) {
        FileReceiver test = new FileReceiver(7779, "receiveTest.txt");
        new Thread(test).run();
    }
}
