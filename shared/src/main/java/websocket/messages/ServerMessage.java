package websocket.messages;

import chess.ChessGame;
import model.GameData;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message = "default message";
    String visitorName;
    String fields = ""; //json string representation of fields
    GameData game = null;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(String visitorName, ServerMessageType type) {
        this.serverMessageType = type;
        this.visitorName = visitorName;
    }

    public ServerMessage(String visitorName, ServerMessageType type, String fields, String message, GameData game) {
        this.serverMessageType = type;
        this.visitorName = visitorName;
        this.fields = fields;
        this.message = message;
        this.game = game;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setGame(GameData game) {
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }

    public String getJsonFields() {
        return this.fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
