package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServiceTests {
    static private DataAccess dataAccess;
    static private Service service;

    @BeforeAll
    public static void init() {
        dataAccess = new MySqlDataAccess();
        service = new Service(dataAccess);
    }

    @AfterEach
    public void reset() {
        try {
            service.clear();
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void positiveRegisterUser()  {
        try {
            UserData user = new UserData("a", "p", "j@p.com");
            AuthData registrationResult = service.registerUser(user);
            Assertions.assertEquals(user.username(), registrationResult.username());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeRegisterUser() {
        try {
            UserData badUser = new UserData("", null, null);
            service.registerUser(badUser);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: bad request");
        }
    }

    @Test
    public void positiveLogin() {
        try {
            UserData user = new UserData("a", "abc", "a@gmail.com");
            AuthData registerResult = service.registerUser(user);
            AuthData loginResult = service.login(user.username(), user.password());
            Assertions.assertNotNull(dataAccess.getAuth(loginResult.authToken()));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogin() {
        try {
            UserData user = new UserData("a", "abc", "a@gmail.com");
            AuthData loginResult = service.login(user.username(), user.password());
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: unauthorized");
        }
    }

    @Test
    public void positiveLogout() {
        try {
            UserData user = new UserData("yoyo", "yoyorocks", "yoyo@hotmail.com");
            AuthData registerResult = service.registerUser(user);
            service.logout(registerResult.authToken());
            Assertions.assertNull(dataAccess.getAuth(registerResult.authToken()));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogout() {
        try {
            String fakeAuth = UUID.randomUUID().toString();
            service.logout(fakeAuth);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: unauthorized");
        }
    }

    @Test
    public void positiveListGames() throws ServiceException {
        UserData user = new UserData("hohoho", "merryChristmas", "");
        AuthData registerResult = service.registerUser(user);
        service.createGame(registerResult.authToken(), "game1");
        Map<String, List<GameData>> games = service.listGames(registerResult.authToken());
        Assertions.assertEquals(games, dataAccess.listGames());
    }

    @Test
    public void negativeListGames() {
        try {
            service.listGames(null);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: bad request");
        }
    }

    @Test
    public void positiveCreateGame() {
        try {
            UserData user = new UserData("programming", "camelCase", "");
            AuthData registerResult = service.registerUser(user);
            GameData game = service.createGame(registerResult.authToken(), "duck");
            Assertions.assertNotNull(game);
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeCreateGame() {
        try {
            UserData user = new UserData("programming", "camelCase", "");
            AuthData registerResult = service.registerUser(user);
            GameData game = service.createGame(registerResult.authToken(), null);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: bad request");
        }
    }

    @Test
    public void positiveJoinGame() {
        try {
            UserData user = new UserData("bob", "bob_party", "bob_party@yahoo.com");
            AuthData registerResult = service.registerUser(user);
            GameData game = service.createGame(registerResult.authToken(), "babushka");
            service.joinGame(registerResult.authToken(), "WHITE", game.gameID());
            ModifiedGameData updatedGame = dataAccess.getGame(game.gameID());
            Assertions.assertNotNull(updatedGame.whiteUsername());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeJoinGame() {
        try {
            UserData user = new UserData("bob", "bob_party", "bob_party@yahoo.com");
            AuthData registerResult = service.registerUser(user);
            GameData game = service.createGame(registerResult.authToken(), "babushka");
            service.joinGame(registerResult.authToken(), "WHITE", game.gameID());

            UserData secondUser = new UserData("bob_deepfake", "bob_deepfake_party", "bob@yahoo.com");
            AuthData secondRegisterResult = service.registerUser(secondUser);
            service.joinGame(secondRegisterResult.authToken(), "WHITE", game.gameID());
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: already taken");
        }
    }

    @Test
    public void positiveClear() {
        try {
            UserData user = new UserData("cohesion", "frank", "");
            AuthData registerResult = service.registerUser(user);
            service.createGame(registerResult.authToken(), "myGame");
            service.createGame(registerResult.authToken(), "nextGame");
            service.clear();
            Map<String, List<GameData>> games = dataAccess.listGames();
            Assertions.assertEquals(games.get("games").size(), 0);
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeClear() {
        try {
            UserData user = new UserData("cohesion", "frank", "");
            AuthData registerResult = service.registerUser(user);
            service.createGame(registerResult.authToken(), "myGame");
            service.createGame(registerResult.authToken(), "nextGame");
            service.clear();
            service.listGames(registerResult.authToken());
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: unauthorized");
        }
    }
}
