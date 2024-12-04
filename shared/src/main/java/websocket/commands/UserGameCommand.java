package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;
    private final String visitorName;
    private final String visitorColor;
    private final String[] moveMade;
    private final ChessGame gameToUpdate;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, String visitorName, String visitorColor,
                String[] moveMade, ChessGame gameToUpdate) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.visitorName = visitorName;
        this.visitorColor = visitorColor;
        this.moveMade = moveMade;
        this.gameToUpdate = gameToUpdate;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String[] getMoveMade() {
        return moveMade;
    }

    public String getVisitorColor() {
        return visitorColor;
    }

    public ChessGame getGameToUpdate() {
        return gameToUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
