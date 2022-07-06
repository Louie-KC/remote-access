import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JFrame;

/**
 * Reads a machines mouse and keyboard inputs, and invokes the clients sendMsg method
 * with the appropriate message for an input event.
 */
public class InputReader implements MouseListener, KeyListener {
    Client client;
    HashMap<Integer, Boolean> activeKeys;

    public InputReader() {
        activeKeys = new HashMap<>(64);
    }

    public InputReader(Client c) {
        this();
        client = c;
    }

    private int convertKeyCode(int keyCode) {
        if (!client.getRemoteOnSameOS()) {
            if (keyCode == KeyEvent.VK_META) { return KeyEvent.VK_WINDOWS; }  // CMD to Start/Win
            if (keyCode == KeyEvent.VK_WINDOWS) { return KeyEvent.VK_META; }  // Start/Win to CMD
        }
        return keyCode;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UNDEFINED) { return; }  // prevent IllegalArg exceptions
        activeKeys.putIfAbsent(e.getKeyCode(), false);
        if (client != null && !activeKeys.get(e.getKeyCode())) {
            // client.sendMsg(new Message<Integer>(Message.Type.KEY_PRESS, e.getKeyCode()));
            client.sendMsg(new Message<Integer>(Message.Type.KEY_PRESS, convertKeyCode(e.getKeyCode())));
            System.out.println("KeyPressed msg sent");
        } else if (!activeKeys.get(e.getKeyCode())) {
            System.out.println("KeyPressed: " + e.getKeyCode());
        }
        activeKeys.replace(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UNDEFINED) { return; }  // prevent IllegalArg exceptions
        // program may be started with a key pressed, which won't be in the map
        activeKeys.putIfAbsent(e.getKeyCode(), true);
        if (client != null) {
            if (!activeKeys.get(e.getKeyCode())) {
                System.out.println("Key " + e.getKeyCode() + " was not active");
                return;
            }
            client.sendMsg(new Message<Integer>(Message.Type.KEY_RELEASE, convertKeyCode(e.getKeyCode())));
        } else {
            System.out.println("KeyReleased: " + e.getKeyCode());
        }
        activeKeys.replace(e.getKeyCode(), false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (client != null) {
            client.sendMsg(new Message<MouseEvent>(Message.Type.MOUSE_CLICK, e));
        } else {
            System.out.println("Mouse press at x: " + e.getX() + ", y: " + e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (client != null) {
            client.sendMsg(new Message<MouseEvent>(Message.Type.MOUSE_RELEASE, e));
        } else {
            System.out.println("Mouse release at x: " + e.getX() + ", y: " + e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        // Testing
        JFrame frame = new JFrame("InputReader test window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
        InputReader test = new InputReader();
        frame.addMouseListener(test);
        frame.addKeyListener(test);
    }
}
