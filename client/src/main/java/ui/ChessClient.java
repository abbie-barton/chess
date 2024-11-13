package ui;

import com.google.gson.Gson;
import model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private String visitorName = null;

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
                case "logout" -> logout(params);
                case "list" -> listGames(params);
                case "create" -> createGame(params);
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
            visitorName = String.join("-", params);
            UserData user = new UserData(params[0], params[1], null);
            server.login(user);
            return String.format("You signed in as %s.", visitorName);
        }
        throw new ResponseException(400, "Expected: <yourname>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            state = State.LOGGED_IN;
            UserData newUser = new UserData(params[0], params[1], params[2]);
            UserData auth = server.createUser(newUser); // should be authData?
            return String.format("You created user with username %s", auth.username());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logout(String ...params) throws ResponseException {
        assertSignedIn();
        server.logout(params[0]);
        state = State.LOGGED_OUT;
        return String.format("%s logged out", visitorName);
    }

    public String listGames(String ...params) throws ResponseException {
        assertSignedIn();
        var games = server.listGames(params[0]);
        var result = new StringBuilder();
        var gson = new Gson();
        for (List<GameData> gameList : games.values()) {
            for (GameData game : gameList) {
                result.append(gson.toJson(game)).append('\n');
            }
        }
        return result.toString();
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 2) {
            GameData game = server.createGame(params[0], params[1]); // should be authData?
            return String.format("You created game with ID %d", game.gameID());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String help() {
        if (state == State.LOGGED_OUT) {
            return """
                    
                      register <USERNAME> <PASSWORD <EMAIL> - to create an account
                      login <USERNAME> <PASSWORD> - to play chess
                      quit - playing chess
                      help - with possible commands
                    """;
        } else if (state == State.LOGGED_IN) {
            return """
                   
                     create <NAME> - a game
                     list - games
                     join <ID> [WHITE|BLACK] - a game
                     observe <ID> - a game
                     logout - when you are done
                     quit - playing chess
                     help - with possible commands
                   """;
        }
        return """
                
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
}
