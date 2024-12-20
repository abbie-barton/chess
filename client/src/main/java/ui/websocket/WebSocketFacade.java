
package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import ui.ResponseException;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;
    String visitorName = "";

    public WebSocketFacade(String url, NotificationHandler notificationHandler, String visitorName) throws ResponseException {
        try {
            this.visitorName = visitorName;
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(String.class, message -> {
                ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                notificationHandler.notify(notification);
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendMessage(UserGameCommand.CommandType type, String authToken, int gameID, String visitorColor,
                            String[] moveMade, ChessGame gameToUpdate) throws ResponseException {
        try {
            var action = new UserGameCommand(type, authToken, gameID, visitorName, visitorColor,
                    moveMade, gameToUpdate, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}