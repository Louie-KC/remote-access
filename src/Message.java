import java.io.Serializable;

public class Message<T> implements Serializable {
    enum Type {
        IMG_REQUEST,
        IMG_RESPONSE,
        KEY_PRESS,
        KEY_RELEASE,
        MOUSE_CLICK,
        MOUSE_RELEASE
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
