import java.io.File;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import java.awt.FileDialog;

public class FileSender implements Runnable {
    private final int FOUR_KB = 1024*4;
    private Socket socket;
    private DataOutputStream dataOutStream;
    private FileInputStream fileInStream;
    private File file;
    private FileDialog fd;

    public FileSender(String targetIP, int targetPort, JFrame window) {
        try {
            System.out.println("FileSender: Making connection");
            socket = new Socket(targetIP, targetPort);
            dataOutStream = new DataOutputStream(socket.getOutputStream());
            fd = new FileDialog(window, "Select file to send", FileDialog.LOAD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sends a file in 4 KB chunks followed by its extension/file type */
    public void sendFile() throws Exception {
        System.out.println("FileSender: Start sending file");
        // Transfer prep data
        dataOutStream.writeLong(file.length());
        dataOutStream.writeUTF(fd.getFile());

        // Data transfer
        byte[] buffer = new byte[FOUR_KB];
        fileInStream = new FileInputStream(file);
        int bytesWritten = 0;
        do { 
            dataOutStream.write(buffer, 0, bytesWritten);
            dataOutStream.flush();
            bytesWritten = fileInStream.read(buffer);
        } while (bytesWritten != -1);
        System.out.println("FileSender: Sending complete");
    }

    /**
     * Opens a file explorer/browser for file selection. The selected file is then loaded
     * into a File instance which is then returned.
     * @return File instance of selected file.
     */
    private File selectFile() {
        if (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) {
            fd.setDirectory("C:\\");
        } else {
            fd.setDirectory("~");
        }
        fd.setVisible(true);
        return new File(fd.getDirectory() + "/" + fd.getFile()); 
    }

    @Override
    public void run() {
        try {
            file = selectFile();
            if (file != null && file.exists()) {
                sendFile();
            } else {
                dataOutStream.writeLong(-1);  // terminate receiver
            }
        } catch (Exception e) {
            System.err.println("Error sending file");
            e.printStackTrace();
        } finally {
            try {
                if (fileInStream != null) { fileInStream.close(); }
                dataOutStream.flush();
                dataOutStream.close();
                socket.close();
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) {
        // Test
        JFrame frame = new JFrame("file browser test");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileSender test = new FileSender("127.0.0.1", 7779, frame);
        new Thread(test).run();
    }
    
}
