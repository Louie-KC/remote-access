import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Reads a machines mouse and keyboard inputs, and invokes the clients sendMsg method
 * with the appropriate message for an input event.
 */
public class InputReader implements MouseListener, MouseWheelListener, KeyListener, Runnable {
    private Client client;
    private HashMap<Integer, Boolean> activeKeys;
    private JPanel panel;  // For mouse position listening
    private boolean mousePresent;
    private Point lastMousePos;

    public InputReader() {
        activeKeys = new HashMap<>(64);
        mousePresent = false;
        lastMousePos = new Point(-1,-1);  // to always be overwritten
    }

    public InputReader(Client c) {
        this();
        client = c;
    }

    /** Sets the panel for mouse position tracking/listening */
    public void setPanel(JPanel inPanel) { panel = inPanel; }

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
    public void mouseEntered(MouseEvent e) { mousePresent = true; }

    @Override
    public void mouseExited(MouseEvent e) { mousePresent = false; }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (client != null) {
            client.sendMsg(new Message<MouseEvent>(Message.Type.MOUSE_SCROLL, e));
        } else {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                System.out.println(e.getUnitsToScroll());
            }
        }
    }

    @Override
    public void run() {
        while (client.isConnected()) {
            Point mousePos = panel.getMousePosition();
            if (mousePresent && mousePos != null && !mousePos.equals(lastMousePos)) {
                client.sendMsg(new Message<Point>(Message.Type.MOUSE_POS, mousePos));
                lastMousePos = mousePos;
            }
            try {
                Thread.sleep(50L);  // target 20 updates per second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }     
    }
    
    public static void main(String[] args) {
        // Testing
        JFrame frame = new JFrame("InputReader test window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
        InputReader test = new InputReader();
        frame.addMouseListener(test);
        frame.addMouseWheelListener(test);
        frame.addKeyListener(test);
    }
}
