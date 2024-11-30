package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        ServerMessage action = new Gson().fromJson(message, ServerMessage.class);
        switch (action.getServerMessageType()) {
//            case LOAD_GAME ->
//            case ERROR ->
            case NOTIFICATION -> notification(action.getVisitorName(), session);
        }
    }

    private void notification(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        String message = String.format("%s, this is a test notification!", visitorName);
        ServerMessage notification = new ServerMessage(visitorName, ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(message);
        connections.broadcast(visitorName, notification);
    }

    public void testBroadcast() throws IOException {
        var message = "THIS IS A TEST BROADCAST. DO NOT PANIC PLEASE";
        var notification = new ServerMessage("test", ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(message);
        connections.broadcast("", notification);
    }

}
