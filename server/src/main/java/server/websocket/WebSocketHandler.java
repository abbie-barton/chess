package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.UnauthorizedException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = "";
        try {
            UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
            username = validateUser(action.getAuthToken());
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.getVisitorName(), action.getAuthToken(),
                        action.getGameID(), action.getVisitorColor(), session);
                // case MAKE_MOVE ->
                // case LEAVE ->
                // case RESIGN ->
            }
        } catch (UnauthorizedException ex) {
            error(username, "Error: unauthorized");
        }
    }

    private void connect(String visitorName, String authToken, int gameID, String color, Session session) throws IOException {
        connections.add(authToken, session);
        String message = String.format("You joined game %s!", gameID);
        Map<String, Object> fields = Map.of("message", message, "authToken", authToken,
                "gameID", gameID, "serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(visitorName,
                ServerMessage.ServerMessageType.LOAD_GAME, json, message);
        // send LOAD_GAME message to root
        connections.alertRoot(visitorName, notification);
        // send NOTIFICATION to other game users
        String notifyMessage = String.format("%s joined the game as %s.", visitorName, color);
        this.notification(visitorName, notifyMessage);
    }

    private void notification(String visitorName, String message) throws IOException {
        Map<String, String> fields = Map.of("message", message);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(visitorName,
                ServerMessage.ServerMessageType.NOTIFICATION, json, message);
        connections.broadcast(visitorName, notification);
    }

    private void error(String username, String message) throws IOException {
        Map<String, Object> fields = Map.of("message", message);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(username,
                ServerMessage.ServerMessageType.ERROR, json, message);
        connections.alertRoot(username, notification);
    }

    private String validateUser(String authToken) throws UnauthorizedException {

    }

}
