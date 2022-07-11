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

    public FileReceiver(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("FileReceiver: waiting for connection");
            socket = serverSocket.accept();
            serverSocket.close();
            dataInStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Reads a file in 4KB chunks and writes to disk at the working directory. 
     * Overwrites any file that shares a name with the name of the file downloaded.
    */
    public void receiveFile() throws Exception {
        System.out.println("FileReceiver: started download");
        // Transfer prep data
        long size = dataInStream.readLong();
        if (size == -1) { return; }  // No file/data selected by sender, stop the method
        String fileName = dataInStream.readUTF();

        // Data receive and disk write
        byte[] buffer = new byte[FOUR_KB];
        fileOutStream = new FileOutputStream(fileName);
        int bytesRead = 0;
        do {
            bytesRead = dataInStream.read(buffer, 0, (int) Math.min(buffer.length, size));
            fileOutStream.write(buffer, 0, bytesRead);
            size -= bytesRead;
        } while (size > 0 && bytesRead != -1);
        System.out.println("Finished downloading file: " + fileName);
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
        // Test
        FileReceiver test = new FileReceiver(7779);
        new Thread(test).run();
    }
}
