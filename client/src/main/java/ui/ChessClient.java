package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.*;
import ui.websocket.WebSocketFacade;
import ui.websocket.NotificationHandler;
import websocket.commands.UserGameCommand;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    public State state = State.LOGGED_OUT;
    private String visitorName = null;
    private String authToken = null;
    private Integer numGames = 0;
    private ModifiedGameData game = null;
    private String visitorColor = null;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;

    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "list" -> listGames();
                case "create" -> createGame(params);
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "leave" -> changeLoginState();
                case "resign" -> resign();
                case "make" -> makeMove(params);
                case "highlight" -> highlight();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            visitorName = params[0];
            UserData user = new UserData(params[0], params[1], null);
            AuthData auth = server.login(user);
            this.authToken = auth.authToken();
            state = State.LOGGED_IN;
            return String.format("You signed in as %s.", visitorName);
        }
        throw new ResponseException(400, "Expected: <yourname>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            UserData newUser = new UserData(params[0], params[1], params[2]);
            AuthData auth = server.createUser(newUser);
            visitorName = params[0];
            this.authToken = auth.authToken();
            state = State.LOGGED_IN;
            return String.format("You created user with username %s", params[1]);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(this.authToken);
        state = State.LOGGED_OUT;
        return String.format("%s logged out", visitorName);
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var games = server.listGames(this.authToken);
        if (games == null) {
            return "No games have been created.";
        }
        StringBuilder returnString = new StringBuilder();
        returnString.append(" ID | Game Name | White Username | Black Username \n");

        for (GameData game : games) {
            returnString.append("  ").append(game.gameID()).append(" - ").append(game.gameName()).append(" - ")
                    .append(game.whiteUsername()).append(" - ").append(game.blackUsername()).append("\n");
            numGames++;
        }
        return returnString.toString();
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            // 1 is a placeholder id, will get replaced
            GameData newGame = new GameData(1, null, null, params[0], new ChessGame());
            GameData game = server.createGame(this.authToken, newGame);
            return String.format("You created game with ID %d", game.gameID());
        }
        throw new ResponseException(400, "Expected: <gameName>");
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException ex) {
            throw new ResponseException(400, "Expected: <gameID> [WHITE|BLACK]");
        }
        if (params.length >= 2) {
            try {
                server.joinGame(this.authToken, params[1].toUpperCase(), gameID);
            } catch (Exception ex) {
                return String.format(ex.getMessage());
            }
            this.state = State.IN_GAME;
            this.visitorColor = params[1].toUpperCase();
            ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
            ws.sendMessage(UserGameCommand.CommandType.CONNECT, this.authToken, gameID, params[1].toUpperCase());
            return String.format("You joined game with ID %d", gameID);
        }
        throw new ResponseException(400, "Expected: <gameID> [WHITE|BLACK]");
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        int gameID = Integer.parseInt(params[0]);
        if (gameID <= 0 || gameID > numGames) {
            return String.format("That game ID does not exist");
        }
        this.state = State.OBSERVE;
        ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
        ws.sendMessage(UserGameCommand.CommandType.CONNECT, this.authToken, gameID, null);
        return String.format("You are observing game with ID %d", gameID);
    }

    private String changeLoginState() throws ResponseException {
        state = State.LOGGED_IN;
        ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
        ws.sendMessage(UserGameCommand.CommandType.LEAVE, this.authToken, game.gameID(), visitorColor);
        return "";
    }

    private String resign() throws ResponseException {
        this.game = new ModifiedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game(), 1);
        ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
        ws.sendMessage(UserGameCommand.CommandType.RESIGN, this.authToken, game.gameID(), visitorColor);
        return "";
    }

    private String makeMove(String... params) throws ResponseException {
        if (game.is_over() == 1) {
            return "Game is over. You cannot make any more moves.";
        }
        return "";
    }

    private String highlight(String... params) throws ResponseException {
        GameData currGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game());
        ChessGame currChessGame = this.game.game();
        Collection<ChessMove> validMoves =
                currChessGame.validMoves(new ChessPosition(Integer.parseInt(params[0]), Integer.parseInt(params[1])));
        // draw board with valid moves highlighted
        this.drawBoard(currGame, validMoves);
        return "";
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

    public void drawBoard(GameData currGame, Collection<ChessMove> validMoves) {
        System.out.println("\n");
        if (state == State.IN_GAME || state == State.OBSERVE) {
            DrawBoard.main(currGame, !Objects.equals(visitorName, currGame.blackUsername()), validMoves, null);
        }
    }

    public void setGame(ModifiedGameData game) {
        this.game = game;
    }

    public String clear() throws ResponseException {
        assertSignedIn();
        server.clear();
        return String.format("You successfully cleared the database.");
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY + """
                    
                        register <USERNAME> <PASSWORD <EMAIL> - to create an account
                        login <USERNAME> <PASSWORD> - to play chess
                        quit - playing chess
                        help - with possible commands
                    """;
        } else if (state == State.LOGGED_IN) {
            return SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY +"""
                   
                       create <NAME> - a game
                       list - games
                       join <ID> [WHITE|BLACK] - a game
                       observe <ID> - a game
                       logout - when you are done
                       quit - playing chess
                       help - with possible commands
                   """;
        } else if (state == State.IN_GAME) {
            return SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY + """
                    redraw - board
                    leave - the chess game
                    make move - <START_POSITION> <END_POSITION>
                    highlight - legal moves
                    resign - the game
                    help - with possible commands
                """;
        }
        return SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY + """
                
                    redraw - board
                    leave - the chess game
                    help - with possible commands
                """;
    }
}
