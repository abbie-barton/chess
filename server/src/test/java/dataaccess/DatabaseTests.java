package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import service.ServiceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DatabaseTests {
    static private DataAccess dataAccess;

    @BeforeEach
    public void init() {
        dataAccess = new MySqlDataAccess();
    }

    @AfterEach
    public void reset() {
        try {
            dataAccess.clear();
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void positiveCreateUser()  {
        UserData user = new UserData("a", "p", "j@p.com");
        UserData result = dataAccess.createUser(user);
        if (result == null) {
            Assertions.fail();
        }
        Assertions.assertEquals(user.username(), result.username());
    }

    @Test
    public void negativeCreateUser() {
        UserData user = new UserData("a", "p", null);
        UserData result = dataAccess.createUser(user);
        Assertions.assertNull(result);
    }

    @Test
    public void positiveGetUser() {
        UserData user = new UserData("austin", "myGardenIsBeautiful", "gardenscapes@scapes.com");
        UserData createResult = dataAccess.createUser(user);
        UserData getResult = dataAccess.getUser(user.username());
        if (getResult == null) {
            Assertions.fail();
        }
        Assertions.assertEquals(getResult.email(), createResult.email());
    }

    @Test
    public void negativeGetUser() {
        UserData user = new UserData("austin", "myGardenIsBeautiful", "gardenscapes@scapes.com");
        UserData createResult = dataAccess.createUser(user);
        UserData getResult = dataAccess.getUser("auztin");
        Assertions.assertNull(getResult);
    }

    @Test
    public void positiveCreateGame() {
        UserData user = new UserData("pumpkin", "orange", "backtothebarnyard@hotmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        String gameName = "pumpy's game";
        GameData gameResult = dataAccess.createGame(gameName);
        Assertions.assertEquals(gameName, gameResult.gameName());
    }

    @Test
    public void negativeCreateGame() {
        UserData user = new UserData("pumpkin", "orange", "backtothebarnyard@hotmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        String gameName = null;
        GameData gameResult = dataAccess.createGame(gameName);
        Assertions.assertNull(gameResult);
    }

    @Test
    public void positiveGetGame() {
        UserData user = new UserData("bobothy", "keyboard", "hotdog@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        String gameName = "dance party";
        GameData createGameResult = dataAccess.createGame(gameName);
        ModifiedGameData getGameResult = dataAccess.getGame(createGameResult.gameID());
        Assertions.assertEquals(createGameResult.gameName(), getGameResult.gameName());
    }

    @Test
    public void negativeGetGame() {
        UserData user = new UserData("bobothy", "keyboard", "hotdog@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        String gameName = "dance party";
        GameData createGameResult = dataAccess.createGame(gameName);
        ModifiedGameData getGameResult = dataAccess.getGame(createGameResult.gameID() + 1);
        Assertions.assertNull(getGameResult);
    }

    @Test
    public void positiveListGames() {
        UserData user = new UserData("tim", "tim+britany", "britim@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        GameData createGameResult = dataAccess.createGame("candle");
        GameData createGameResult2 = dataAccess.createGame("wick");
        Map<String, List<GameData>> games = dataAccess.listGames();
        Assertions.assertEquals(games.get("games").getFirst().gameName(), createGameResult.gameName());
    }

    @Test
    public void negativeListGames() {
        UserData user = new UserData("tim", "tim+britany", "britim@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        GameData createGameResult = dataAccess.createGame("candle");
        GameData createGameResult2 = dataAccess.createGame("wick");

        // drop table
        try (var conn = configureDatabase()) {
            var statement = "DROP TABLE game";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            Assertions.fail();
        }

        Map<String, List<GameData>> games = dataAccess.listGames();
        Assertions.assertNull(games);
    }

    @Test
    public void positiveUpdateGame() {
        UserData user = new UserData("lotion", "smooth", "silky@yahoo.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        GameData createGameResult = dataAccess.createGame("aloe");
        dataAccess.updateGame(createGameResult.gameID(), "WHITE", createResult.username());
        ModifiedGameData getGameResult = dataAccess.getGame(createGameResult.gameID());
        Assertions.assertNotEquals(createGameResult.whiteUsername(), getGameResult.whiteUsername());
    }

    @Test
    public void negativeUpdateGame() {
        UserData user = new UserData("lotion", "smooth", "silky@yahoo.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        GameData createGameResult = dataAccess.createGame("aloe");

        // drop table
        try (var conn = configureDatabase()) {
            var statement = "DROP TABLE game";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            Assertions.fail();
        }

        dataAccess.updateGame(createGameResult.gameID(), "WHITE", createResult.username());
        ModifiedGameData getGameResult = dataAccess.getGame(createGameResult.gameID());
        Assertions.assertNull(getGameResult);
    }

    @Test
    public void positiveCreateAuth() {
        UserData user = new UserData("Stampy", "theElephant", "eleph.stampy@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        Assertions.assertNotNull(authResult.authToken());
    }

    @Test
    public void negativeCreateAuth() {
        AuthData authResult = dataAccess.createAuth(null);
        Assertions.assertNull(authResult);
    }

    @Test
    public void positiveGetAuth() {
        UserData user = new UserData("Yoyoboy", "stringIn,stringOut", "YOyoy@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(user.username());
        AuthData getAuthResult = dataAccess.getAuth(authResult.authToken());
        Assertions.assertEquals(authResult, getAuthResult);
    }

    @Test
    public void negativeGetAuth() {
        AuthData getAuthResult = dataAccess.getAuth(null);
        Assertions.assertNull(getAuthResult);
    }

    @Test
    public void positiveDeleteAuth() throws ServiceException {
        UserData user = new UserData("AAAHH", "noChill", "typeA@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(createResult.username());
        try {
            dataAccess.deleteAuth(authResult.authToken());
        } catch (Exception ex) {
            throw new ServiceException("");
        }
        AuthData getAuthResult = dataAccess.getAuth(authResult.authToken());
        Assertions.assertNull(getAuthResult);
    }

    @Test
    public void negativeDeleteAuth() {
        UserData user = new UserData("AAAHH", "noChill", "typeA@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        AuthData authResult = dataAccess.createAuth(createResult.username());

        // drop table
        try (var conn = configureDatabase()) {
            var statement = "DROP TABLE auth";
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            Assertions.fail();
        }

        try {
            dataAccess.deleteAuth(authResult.authToken());
        } catch (Exception ex) {
            Assertions.assertEquals(ex.getMessage(), "Error: bad request");
        }
    }

    @Test
    public void positiveClearDatabase() {
        UserData user = new UserData("psohtse", "sdfklk", "zoi2fj@gmail.com");
        UserData createResult = dataAccess.createUser(user);
        dataAccess.clear();
        UserData getResult = dataAccess.getUser(createResult.username());
        Assertions.assertNull(getResult);
    }

    private Connection configureDatabase() {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                Properties props = new Properties();
                props.load(propStream);
                String databaseName = props.getProperty("db.name");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                String connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);

                var conn = DriverManager.getConnection(connectionUrl, user, password);
                conn.setCatalog(databaseName);
                return conn;
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }
}
