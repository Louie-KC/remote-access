import java.util.Optional;

public class Message<T> {
    private String type;
    private T data;
    private Optional<String> piggyback;

    // Constructors
    public Message(String msgType, T msgData) {
        type = msgType;
        data = msgData;
        piggyback = Optional.empty();
    }

    public Message(String msgType, T msgData, String msgPiggyback) {
        this(msgType, msgData);
        piggyback = Optional.of(msgPiggyback);
    }

    // Methods
    public String getType() { return type; }

    public T getData() { return data; }

    public boolean hasPiggyback() {
        if (piggyback.isPresent()) {
            return true;
        }
        return false;
    }

    public String getPiggyback() { 
        if (piggyback.isPresent() && !piggyback.get().isBlank()) {
            return piggyback.get();
        }
        return "";
    }

}
