package ui;

import chess.ChessGame;
import model.*;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    public State state = State.LOGGED_OUT;
    private String visitorName = null;
    private String authToken = null;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
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
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            state = State.LOGGED_IN;
            visitorName = params[0];
            UserData user = new UserData(params[0], params[1], null);
            AuthData auth = server.login(user);
            this.authToken = auth.authToken();
            return String.format("You signed in as %s.", visitorName);
        }
        throw new ResponseException(400, "Expected: <yourname>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            state = State.LOGGED_IN;
            UserData newUser = new UserData(params[0], params[1], params[2]);
            AuthData auth = server.createUser(newUser);
            this.authToken = auth.authToken();
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
        returnString.append(" ID | Game Name \n");
        for (GameData game : games) {
            returnString.append("  ").append(game.gameID()).append(" - ").append(game.gameName()).append("\n");
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
        int gameID = Integer.parseInt(params[0]);
        if (params.length >= 2) {
            try {
                server.joinGame(this.authToken, params[1].toUpperCase(), gameID);
            } catch (Exception ex) {
                return String.format(ex.getMessage());
            }
            GameData testGame = new GameData(1, null, null, "testGame", new ChessGame());
            this.drawBoard(testGame);
            this.state = State.IN_GAME;
            return String.format("You joined game with ID %d", gameID);
        }
        throw new ResponseException(400, "Expected: <gameID> [WHITE|BLACK]");
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        int gameID = Integer.parseInt(params[1]);
        return String.format("You are observing game with ID %d", gameID);
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
        }
        return SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY + """
                
                    redraw - board
                    quit - playing chess
                    help - with possible commands
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

    private void drawBoard(GameData game) {
        DrawBoard.main(game, true);
        System.out.print("\n");
        DrawBoard.main(game, false);
    }
}
