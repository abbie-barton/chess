package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private int nextGameId = 1;
    final private Map<String, UserData> users = new HashMap<>();
    final private Map<String, AuthData> auth = new HashMap<>();
    final private Map<String, GameData> games = new HashMap<>();

    @Override
    public UserData getUser(String userName) {
        return users.get(userName);
    }

    @Override
    public UserData createUser(UserData newUser) {
        UserData user = new UserData(newUser.username(), newUser.password(), newUser.email());

        users.put(newUser.username(), user);
        return user;
    }

    @Override
    public GameData getGame(String gameID) {
        return games.get(gameID);
    }

    @Override
    public GameData createGame(String gameName) {
        GameData game = new GameData(nextGameId++, null, null, gameName, new ChessGame());

        games.put(gameName, game);
        return game;
    }

    @Override
    public Collection<GameData> listGames() {
        return new ArrayList<>();
    }

    @Override
    public void updateGame(String gameID, String playerColor, String username) {
        // update game
    }

    @Override
    public AuthData createAuth(String username) {
        return new AuthData("2sldkfj", username);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auth.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        // delete token
    }

    @Override
    public void clear() {
        users.clear();
        auth.clear();
        games.clear();
    }

}
