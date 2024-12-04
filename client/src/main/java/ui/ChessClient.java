package ui;

import chess.*;
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
                case "highlight" -> highlight(params);
                case "redraw" -> redraw();
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
        throw new ResponseException(400, "Expected: <username> <password>");
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
            ws.sendMessage(UserGameCommand.CommandType.CONNECT, this.authToken, gameID,
                    params[1].toUpperCase(), null, null);
            return String.format("You joined game with ID %d\n", gameID);
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
        ws.sendMessage(UserGameCommand.CommandType.CONNECT, this.authToken, gameID,
                null, null, null);
        return String.format("You are observing game with ID %d", gameID);
    }

    private String changeLoginState() throws ResponseException {
        state = State.LOGGED_IN;
        ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
        ws.sendMessage(UserGameCommand.CommandType.LEAVE, this.authToken, game.gameID(),
                visitorColor, null, null);
        return "";
    }

    private String resign() throws ResponseException {
        this.game = new ModifiedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game(), 1);
        this.state = State.LOGGED_IN;
        ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
        ws.sendMessage(UserGameCommand.CommandType.RESIGN, this.authToken, game.gameID(),
                visitorColor, null, null);
        return "You resigned from the game.";
    }

    private String makeMove(String... params) throws ResponseException {
        if (game.is_over() == 1) {
            return "Game is over. You cannot make any more moves.";
        }

        int[] startRowAndCol = convertTextPosition(0, params);
        int[] endRowAndCol = convertTextPosition(1, params);
        ChessGame currGame = this.game.game();

        // get the promotion piece if there
        ChessPiece.PieceType type = null;
        if (params[2] != null) {
            try {
                type = findPieceType(params[2]);
            } catch (Exception ex) {
                return ex.getMessage();
            }
        }

        // try making the move - if an exception is caught, then move was invalid and was not made
        ChessMove potentialMove = new ChessMove(new ChessPosition(startRowAndCol[0], startRowAndCol[1]),
                new ChessPosition(endRowAndCol[0], endRowAndCol[1]), type);
        try {
            currGame.makeMove(potentialMove);
            // make move in database
//            server.updateGame();
            ws = new WebSocketFacade(serverUrl, notificationHandler, visitorName);
            ws.sendMessage(UserGameCommand.CommandType.MAKE_MOVE, this.authToken, game.gameID(),
                    visitorColor, new String[] {params[0], params[1]}, currGame);
            return String.format("Move made: %s to %s", params[0], params[1]);
        } catch (InvalidMoveException ex) {
            return "Invalid move. Try <highlight> to see valid moves";
        }
    }

    private String highlight(String... params) throws ResponseException {
        int[] rowAndCol = convertTextPosition(0, params);
        if (rowAndCol.length == 0) {
            return String.format("Expected: <START_POSITION>");
        }
        GameData currGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game());
        ChessGame currChessGame = this.game.game();

        Collection<ChessMove> validMoves =
                currChessGame.validMoves(new ChessPosition(rowAndCol[0], rowAndCol[1]));
        if (validMoves.isEmpty()) {
            return String.format("No valid moves for %s", params[0]);
        }
        // draw board with valid moves highlighted
        this.drawBoard(currGame, validMoves, new ChessPosition(rowAndCol[0], rowAndCol[1]));
        return "";
    }

    public String redraw() throws ResponseException {
        GameData currGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game());
        this.drawBoard(currGame, null, null);
        return "";
    }

    private ChessPiece.PieceType findPieceType(String type) throws Exception {
        return switch (type.toLowerCase()) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "pawn" -> ChessPiece.PieceType.PAWN;
            default -> throw new Exception("Unexpected value: " + type.toLowerCase() +
                    "\n   Options: <queen> <rook> <bishop> <knight> <pawn>");
        };
    }

    private int[] convertTextPosition(int index, String... params) {
        if (params.length == 0) {
            return new int[] {};
        }
        char charRow = params[index].charAt(0);
        int intRow = 1;
        switch(charRow) {
            case 'b' -> intRow = 2;
            case 'c' -> intRow = 3;
            case 'd' -> intRow = 4;
            case 'e' -> intRow = 5;
            case 'f' -> intRow = 6;
            case 'g' -> intRow = 7;
            case 'h' -> intRow = 8;
        }
        int col = params[index].charAt(1) - '0';
        return new int[]{ col, intRow };
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

    public void drawBoard(GameData currGame, Collection<ChessMove> validMoves, ChessPosition startPosition) {
        System.out.println("\n");
        if (state == State.IN_GAME || state == State.OBSERVE) {
            DrawBoard.main(currGame, !Objects.equals(visitorName, currGame.blackUsername()), validMoves, startPosition);
        }
    }

    public void setGame(ModifiedGameData game) {
        this.game = game;
    }

    public ModifiedGameData getGame() {
        return this.game;
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
                    make move - <START_POSITION> <END_POSITION> <PROMOTION_PIECE (optional)>
                    highlight <START_POSITION> - legal moves
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
