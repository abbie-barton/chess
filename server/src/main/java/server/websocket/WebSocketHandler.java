package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import model.ModifiedGameData;
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
            switch (action.getCommandType()) {
                case CONNECT -> connect(username, action.getGameID(), action.getVisitorColor(), session);
                case MAKE_MOVE -> makeMove(username, action.getGameID(), action.getMoveMade(), action.getMove(), session);
                case LEAVE -> leave(username, action.getGameID(), action.getVisitorColor(), session);
                case RESIGN -> resign(username, action.getGameID(), action.getVisitorColor(), session);
            }
        } catch (UnauthorizedException ex) {
            error(username,"Error: unauthorized", session);
        } catch (Exception ex) {
            error(username,"Error: " + ex.getMessage(), session);
        }
    }

    private void connect(String visitorName, int gameID, String color, Session session) throws IOException {
        try {
            connections.add(visitorName, session, gameID);
            ServerMessage notification = getGameNotification(visitorName, gameID);
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
            error(visitorName,"Error: Game not found", session);
        }
    }

    private void makeMove(String visitorName, int gameID, String[] moveMade, ChessMove move, Session session) throws IOException {
        try {
            ModifiedGameData game = getGame(gameID);
            if (game.is_over() == 1) {
                throw new UnauthorizedException(200, "Error: game is over. You cannot make any more moves.");
            }
            if (move != null) {
                ChessGame.TeamColor currColor = getCurrColor(visitorName, game);
                ChessPosition startPosition = move.getStartPosition();
                ChessPiece startPiece = game.game().getBoard().getPiece(new ChessPosition(startPosition.getRow(),
                        startPosition.getColumn()));
                if (startPiece != null) {
                    if (startPiece.getTeamColor() != currColor) {
                        throw new UnauthorizedException(200, "Error: You can't move that piece!");
                    }
                }
                // check if move is valid
                game.game().makeMove(new ChessMove(move.getStartPosition(), move.getEndPosition(), null));
                updateGameMoves(game.gameID(), game.game());
            }
            ServerMessage notification = getGameNotification(visitorName, gameID);
            // send LOAD_GAME message to root
            connections.alertRoot(visitorName, notification);

            // send LOAD_GAME message to everyone
            connections.broadcast(visitorName, notification, gameID);

            // send move made notification
            String notifyMessage;
            if (moveMade != null) {
                notifyMessage = String.format("%s made a move: %s to %s", visitorName, moveMade[0], moveMade[1]);
            } else {
                notifyMessage = String.format("%s made a move!", visitorName);
            }
            this.notification(visitorName, gameID, notifyMessage);

            // check if game in check or checkmate
            ChessGame.TeamColor color = game.game().getTeamTurn();
            // use opposite color of current color
            ChessGame.TeamColor oppositeColor;
            if (color == ChessGame.TeamColor.WHITE) {
                oppositeColor = ChessGame.TeamColor.BLACK;
            } else {
                oppositeColor = ChessGame.TeamColor.WHITE;
            }

            if (game.game().isInCheckmate(oppositeColor)) {
                sendToAll(visitorName, String.format("%s is in checkmate. Game is over.", oppositeColor), gameID);
                markGameAsOver(gameID);
            } else if (game.game().isInCheck(oppositeColor)) {
                sendToAll(visitorName, String.format("%s is in check.", oppositeColor), gameID);
            } else if (game.game().isInStalemate(oppositeColor)) {
                sendToAll(visitorName, "Game is in stalemate. Game is over.", gameID);
                markGameAsOver(gameID);
            }
        } catch (Exception ex) {
            error(visitorName, ex.getMessage(), session);
        }
    }

    private void leave(String visitorName, int gameID, String color, Session session) throws IOException {
        try {
            ModifiedGameData game = getGame(gameID);
            ChessGame.TeamColor currColor = getCurrColor(visitorName, game);
            if (currColor != null) {
                // player is a player
                removeGamePlayer(gameID, currColor);
            }

            connections.remove(visitorName);

            String notifyMessage = String.format("%s left the game.", visitorName);
            this.notification(visitorName, gameID, notifyMessage);
        } catch (Exception ex) {
            error(visitorName, "Error: " + ex.getMessage(), session);
        }
    }

    private void resign(String visitorName, int gameID, String color, Session session) throws IOException {
        try {
            ModifiedGameData game = getGame(gameID);
            if (game.is_over() == 1) {
                throw new UnauthorizedException(200, "Error: game is already over.");
            }
            if (getCurrColor(visitorName, game) == null) {
                throw new UnauthorizedException(200, "Error: you are not a player in the game.");
            }
            markGameAsOver(gameID);

            Map<String, Object> fields = Map.of(visitorName, visitorName, "gameID", gameID, "message", "You resigned from the game",
                    "serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            var json = new Gson().toJson(fields);
            ServerMessage notification = new ServerMessage(visitorName,
                    ServerMessage.ServerMessageType.NOTIFICATION, json, "You resigned from the game.", null);
            connections.alertRoot(visitorName, notification);

            String notifyMessage = String.format("%s resigned from the game.", visitorName);
            this.notification(visitorName, gameID, notifyMessage);
        } catch (Exception ex) {
            error(visitorName, "Error: " + ex.getMessage(), session);
        }
    }

    private void notification(String visitorName, Integer gameID, String message) throws IOException {
        Map<String, Object> fields = Map.of("message", message, "visitorName", visitorName, "gameID", gameID,
                "serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(visitorName,
                ServerMessage.ServerMessageType.NOTIFICATION, json, message, null);
        connections.broadcast(visitorName, notification, gameID);
    }

    private void error(String username, String message, Session session) throws IOException {
        Map<String, Object> fields = Map.of("errorMessage", message, "visitorName", username,
                "serverMessageType", ServerMessage.ServerMessageType.ERROR);
        var json = new Gson().toJson(fields);
        ServerMessage notification = new ServerMessage(username,
                ServerMessage.ServerMessageType.ERROR, json, message, null);
        if (session.isOpen()) {
            // only send to root
            session.getRemote().sendString(notification.getJsonFields());
        }
    }

    private String getAuth(String authToken) throws UnauthorizedException {
        var username = service.getAuth(authToken);
        if (username == null) {
            throw new UnauthorizedException(401, "Error: unauthorized");
        } else {
            return username;
        }
    }

    private ModifiedGameData getGame(int gameID) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new ServiceException("Error: Game does not exist");
        } else {
            return game;
        }
    }

    private void removeGamePlayer(int gameID, ChessGame.TeamColor color) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new ServiceException("Error: Game does not exist");
        } else {
            try {
                String stringColor;
                if (color == ChessGame.TeamColor.WHITE) {
                    stringColor = "WHITE";
                } else if (color == ChessGame.TeamColor.BLACK) {
                    stringColor = "BLACK";
                } else {
                    throw new ServiceException("Error: You are not a player in the game");
                }
                service.removeGamePlayer(gameID, stringColor);
            } catch (Exception ex) {
                throw new ServiceException("Error: " + ex.getMessage());
            }
        }
    }

    private void markGameAsOver(int gameID) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new Exception();
        } else {
            try {
                service.markGameAsOver(gameID);
            } catch (Exception ex) {
                throw new Exception();
            }
        }
    }

    private void updateGameMoves(int gameID, ChessGame updatedGame) throws Exception {
        var game = service.getGame(gameID);
        if (game == null) {
            throw new Exception();
        } else {
            try {
                service.updateMoves(gameID, updatedGame);
            } catch (Exception ex) {
                throw new Exception();
            }
        }
    }

    private ServerMessage getGameNotification(String visitorName, int gameID) throws Exception {
        try {
            // set chessGame for gameID
            ModifiedGameData game = getGame(gameID);

            Map<String, Object> fields = Map.of("visitorName", visitorName, "gameID", gameID,
                    "serverMessageType", ServerMessage.ServerMessageType.LOAD_GAME, "game", game);
            var json = new Gson().toJson(fields);
            return new ServerMessage(visitorName,
                    ServerMessage.ServerMessageType.LOAD_GAME, json, null, game);
        } catch (Exception ex) {
            throw new Exception();
        }
    }

    private void sendToAll(String visitorName, String message, int gameID) throws Exception {
        try {
            Map<String, Object> fields = Map.of("message", message, "visitorName", visitorName,
                    "serverMessageType", ServerMessage.ServerMessageType.NOTIFICATION);
            var json = new Gson().toJson(fields);
            ServerMessage notification = new ServerMessage(visitorName,
                    ServerMessage.ServerMessageType.NOTIFICATION, json, message, null);
            connections.broadcast(visitorName, notification, gameID);
            connections.alertRoot(visitorName, notification);
        } catch (Exception ex) {
            throw new Exception();
        }
    }

    private ChessGame.TeamColor getCurrColor(String visitorName, ModifiedGameData game) throws Exception {
        if (visitorName.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (visitorName.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }

}
