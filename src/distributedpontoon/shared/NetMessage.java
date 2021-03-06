package distributedpontoon.shared;

import distributedpontoon.directoryservice.DirectoryService;
import distributedpontoon.server.MultiPlayerGame;
import distributedpontoon.server.SinglePlayerGame;
import distributedpontoon.shared.IServerGame;
import java.io.Serializable;

/**
 * Generic message class to send data between two networked objects. Allows the 
 * specification of a {@link MessageType} to help the receiving object 
 * understand the contents of the message.
 * 
 * @param <T> The object type to store inside this message.
 * @author 6266215
 * @version 1.0
 * @since 2015-02-04
 */
public class NetMessage<T> implements Serializable {
    
    /**
     * Various types of {@link NetMessage}.
     * 
     * @version 1.2
     * @since 2015-02-06
     */
    public static enum MessageType
    {
        /** Sent to a {@link DirectoryService} to store this server on it. */
        REGISTER_SERVER,
        /** Sent by a {@link MultiPlayerGame} to register it as running. */
        REGISTER_GAME,
        /** Sent by a {@link MultiPlayerGame} to register it has ended. */
        UNREGISTER_GAME,
        /** Sent to a {@link DirectoryService} to request a list of servers. */
        QUERY_SERVERS,
        /** Sent from a {@link DirectoryService} to see if servers are up. */
        POLL_SERVER,
        /** Sent by a client when it requests to play a new
         * {@link SinglePlayerGame} */
        CLIENT_JOIN_SP,
        /** Sent by a client when it requests to play a existing or new 
         * {@link MultiPlayerGame} */
        CLIENT_JOIN_MP,
        /** Sent to {@link IServerGame}s by a client when they can play. */
        CLIENT_READY,
        /** Sent to clients to let them know the server knows them. */
        JOIN_ACKNOWLEDGE,
        /** Sent when a game initialises. */
        GAME_INITIALISE,
        /** Sent by players when they are ready to accept 
         * {@link MessageType#TURN_NOTIFY} messages. */
        PLAYER_READY,
        /** Sent to clients to tell them it's their turn. */
        TURN_NOTIFY,
        /** Received from clients with their move for the turn. */
        TURN_RESPONSE,
        /** Messages with this type will contain {@link Card} objects. */
        CARD_TRANSFER,
        /** Sent to clients at the end of a game to adjust their balance. */
        GAME_RESULT,
        /** Tells the server that the client wishes to disconnect. */
        CLIENT_DISCONNECT,
        UPDATE_BANK;
    }
    
    /** The type of message being sent as an {@link MessageType} */
    public final MessageType Type;
    /** The contents of the message typed to T. */
    public final T Contents;
    /** The unique ID for this message. */
    public final long MessageID;
    /** The total number of messages created. */
    public static volatile long MessageCount;
    /** Serialisation ID. */
    private static final long serialVersionUID = -23198619128710267L;
    
    /**
     * Creates a new {@link NetMessage} with the specified {@link MessageType} 
     * and the provided object stored inside.
     * 
     * @param type The {@link MessageType} of this message.
     * @param contents The object to store typed to T.
     * @since 1.0
     */
    public NetMessage(MessageType type, T contents)
    {
        this.Type = type;
        this.Contents = contents;
        synchronized(this) {
            NetMessage.MessageCount++;
            this.MessageID = NetMessage.MessageCount;
        }
    }
}
