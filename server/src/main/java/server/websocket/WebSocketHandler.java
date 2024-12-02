package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.UnauthorizedException;
import service.Service;
import service.ServiceException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess = new MySqlDataAccess();
    private final Service service = new Service(dataAccess);

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = "";
        try {
            UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
            username = getAuth(action.getAuthToken());
            if (username == null) {
                username = action.getVisitorName();
            }
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.getVisitorName(), action.getAuthToken(),
                        action.getGameID(), action.getVisitorColor(), session);
                // case MAKE_MOVE ->
                case LEAVE -> leave(username, action.getGameID(), action.getVisitorColor());
                case RESIGN -> resign(username, action.getGameID(), action.getVisitorColor());
            }
        } catch (UnauthorizedException ex) {
            error(username,"Error: unauthorized");
        } catch (Exception ex) {
            error(username,"Error: " + ex.getMessage());
        }
    }

    private void connect(String visitorName, String authToken, int gameID, String color, Session session) throws IOException {
        try {
            connections.add(visitorName, session);
            String message = String.format("You joined game %s!", gameID);
            // set chessGame for gameID
            GameData game = getGame(gameID);

            Map<String, Object> fields = Map.of("message", message, "visitorName", visitorName, "gameID", gameID,
                    "serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME, "game", game);
            var json = new Gson().toJson(fields);
            ServerMessage notification = new ServerMessage(visitorName,
                    ServerMessage.ServerMessageType.LOAD_GAME, json, message, game);

            // send LOAD_GAME message to root
            connections.alertRoot(visitorName, notification);

            String notifyMessage;
            if (color == null) {
                // the one who connected is an observer - send NOTIFICATION
                notifyMessage = String.format("%s joined the game as an observer.", visitorName);
            } else {
                // send NOTIFICATION to other game users
                notifyMessage = String.format("%s joined the game as %s.", visitorName, color);
            }
            this.notification(visitorName, gameID, notifyMessage);
        } catch (Exception ex) {
            error(visitorName,"Error: Game not found");
        }
    }

    private void leave(String visitorName, int gameID, String color) throws IOException {
        try {
            removeGamePlayer(gameID, color.toUpperCase());
            String message = String.format("You left game %s!", gameID);
            Map<String, Object> fields = Map.of("message", message, "visitorName", visitorName, "gameID", gameID,
                    "serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            var json = new Gson().toJson(fields);
            ServerMessage notification = new ServerMessage(visitorName,
                    ServerMessage.ServerMessageType.NOTIFICATION, json, message, null);
            connections.alertRoot(visitorName, notification);

            connections.remove(visitorName);

            String notifyMessage = String.format("%s left the game.", visitorName);
            this.notification(visitorName, gameID, notifyMessage);
        } catch (Exception ex) {
            error(visitorName, "Error: " + ex.getMessage());
        }
    }

    private void resign(String visitorName, int gameID, String color) throws IOException {

    }

    private void notification(String visitorName, Integer gameID, String message) throws IOException {
        Map<String, Object> fields = Map.of("message", message, "visitorName", visitorName, "gameID", gameID,
                "serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(visitorName,
                ServerMessage.ServerMessageType.NOTIFICATION, json, message, null);
        connections.broadcast(visitorName, notification);
    }

    private void error(String username, String message) throws IOException {
        Map<String, Object> fields = Map.of("message", message, "visitorName", username,
                "serverMessageType", ServerMessage.ServerMessageType.ERROR);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(username,
                ServerMessage.ServerMessageType.ERROR, json, message, null);
        connections.alertRoot(username, notification);
    }

    private String getAuth(String authToken) throws UnauthorizedException {
        var username = service.getAuth(authToken);
        if (username == null) {
            throw new UnauthorizedException(401, "Error: unauthorized");
        } else {
            return username;
        }
    }

    private GameData getGame(int gameID) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new ServiceException("Error: Game does not exist");
        } else {
            return game;
        }
    }

    private void removeGamePlayer(int gameID, String colorToRemove) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new ServiceException("Error: Game does not exist");
        } else {
            try {
                service.removeGamePlayer(gameID, colorToRemove);
            } catch (Exception ex) {
                throw new ServiceException("Error: " + ex.getMessage());
            }
        }
    }

    private void markGameAsOver(int gameID) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new ServiceException("Error: Game does not exist");
        } else {
            try {
                service.markGameAsOver(gameID);
            } catch (Exception ex) {
                throw new ServiceException("Error: " + ex.getMessage());
            }
        }
    }

}
