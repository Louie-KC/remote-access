import java.io.Serializable;

public class Message<T> implements Serializable {
    enum Type {
        INFO,
        IMG_REQUEST,
        IMG_RESPONSE,
        IMG_NO_UPDATE,
        KEY_PRESS,
        KEY_RELEASE,
        MOUSE_POS,
        MOUSE_CLICK,
        MOUSE_RELEASE,
        MOUSE_SCROLL,
        FILE_INIT,
        FILE_REQ,
        CLIPBOARD_DATA,
        CLIPBOARD_REQUEST,
        EXIT
    }

    private Type type;
    private T data;

    // Constructor
    public Message(Type msgType, T msgData) {
        type = msgType;
        data = msgData;
    }

    // Methods
    public Type getType() { return type; }

    public T getData() { return data; }

}
