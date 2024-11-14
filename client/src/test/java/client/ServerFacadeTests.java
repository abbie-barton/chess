package client;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    static private DataAccess dataAccess;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
        dataAccess = new MySqlDataAccess();
    }

    @AfterEach
    public void reset() {
        try {
            serverFacade.clear();
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void positiveCreateUser() {
        UserData newUser = new UserData("a", "a", "a");
        try {
            AuthData returnedAuth = serverFacade.createUser(newUser);
            Assertions.assertNotNull(returnedAuth);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeCreateUser() {
        UserData newUser = new UserData(null, null, null);
        try {
            AuthData returnedAuth = serverFacade.createUser(newUser);
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

    @Test
    public void positiveLogin() {
        UserData newUser = new UserData("b", "b", "b");
        try {
            AuthData returnedAuth = serverFacade.createUser(newUser);
            AuthData loginAuth = serverFacade.login(newUser);
            Assertions.assertNotNull(loginAuth);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogin() {
        UserData newUser = new UserData("b", "b", "b");
        try {
            serverFacade.createUser(newUser);
            serverFacade.login(new UserData("7", "s", "f"));
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

    @Test
    public void positiveLogout() {
        UserData newUser = new UserData("c", "c", "c");
        try {
            serverFacade.createUser(newUser);
            AuthData loginAuth = serverFacade.login(newUser);
            serverFacade.logout(loginAuth.authToken());
            Assertions.assertNull(dataAccess.getAuth(loginAuth.authToken()));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogout() {
        UserData newUser = new UserData("d", "d", "d");
        try {
            serverFacade.createUser(newUser);
            serverFacade.logout("fake auth token");
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

    @Test
    public void positiveListGames() {
        UserData newUser = new UserData("e", "e", "e");
        try {
            AuthData loginAuth = serverFacade.createUser(newUser);
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            serverFacade.createGame(loginAuth.authToken(), newGame);
            List<GameData> games = serverFacade.listGames(loginAuth.authToken());
            Assertions.assertNotNull(games);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeListGames() {
        UserData newUser = new UserData("e", "e", "e");
        try {
            AuthData loginAuth = serverFacade.createUser(newUser);
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            serverFacade.createGame(loginAuth.authToken(), newGame);
            serverFacade.listGames("fake auth token");
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

    @Test
    public void positiveCreateGame() {
        UserData newUser = new UserData("f", "f", "f");
        try {
            AuthData loginAuth = serverFacade.createUser(newUser);
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            GameData game = serverFacade.createGame(loginAuth.authToken(), newGame);
            Assertions.assertNotNull(game);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeCreateGame() {
        try {
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            GameData game = serverFacade.createGame("fake auth token", newGame);
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

    @Test
    public void positiveJoinGame() {
        UserData newUser = new UserData("g", "g", "g");
        try {
            AuthData loginAuth = serverFacade.createUser(newUser);
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            GameData game = serverFacade.createGame(loginAuth.authToken(), newGame);
            serverFacade.joinGame(loginAuth.authToken(), "WHITE", game.gameID());
            GameData gotGame = dataAccess.getGame(game.gameID());
            Assertions.assertNotNull(gotGame.whiteUsername());
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeJoinGame() {
        UserData newUser = new UserData("g", "g", "g");
        try {
            AuthData loginAuth = serverFacade.createUser(newUser);
            GameData newGame = new GameData(1, null, null, "newGame", new ChessGame());
            GameData game = serverFacade.createGame(loginAuth.authToken(), newGame);
            serverFacade.joinGame(loginAuth.authToken(), "WHITE", game.gameID());
            serverFacade.joinGame(loginAuth.authToken(), "WHITE", game.gameID());
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

}
