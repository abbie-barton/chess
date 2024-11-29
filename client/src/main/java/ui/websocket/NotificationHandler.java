package ui.websocket;

import websocket.messages.ServerMessage.ServerMessageType;

public interface NotificationHandler {
    void notify(ServerMessageType notification);
}
