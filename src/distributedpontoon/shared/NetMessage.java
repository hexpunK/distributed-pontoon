package distributedpontoon.shared;

import java.io.Serializable;

/**
 *
 * @author Jordan
 */
public class NetMessage<T> implements Serializable {
    
    public static enum MessageType implements Serializable
    {
        GAME_INITIALISE,
        TURN_NOTIFY,
        TURN_RESPONSE,
        CARD_TRANSFER,
        GAME_RESULT;
    }
    
    public final MessageType Type;
    public final T Contents;
    public static volatile long MessageID;
    
    public NetMessage(MessageType type, T contents)
    {
        this.Type = type;
        this.Contents = contents;
        synchronized(this) {
            NetMessage.MessageID++;
        }
    }
}
