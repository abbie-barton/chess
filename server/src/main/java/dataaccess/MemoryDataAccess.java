package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private int nextGameId = 1;
    final private Map<String, UserData> users = new HashMap<>();
    final private Map<String, AuthData> auth = new HashMap<>();
    final private Map<Integer, GameData> games = new HashMap<>();

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
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public GameData createGame(String gameName) {
        GameData game = new GameData(nextGameId++, null, null, gameName, new ChessGame());

        games.put(nextGameId, game);
        return game;
    }

    @Override
    public Collection<GameData> listGames() {
        return new ArrayList<>();
    }

    @Override
    public void updateGame(int gameID, String playerColor, String username) {
        // update game
        GameData game = games.get(gameID);
        if (Objects.equals(playerColor, "WHITE")) {
            games.put(gameID, new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game()));
        } else {
            games.put(gameID, new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game()));
        }
    }

    @Override
    public AuthData createAuth(String username) {
        AuthData newAuth = new AuthData(UUID.randomUUID().toString(), username);
        auth.put(newAuth.authToken(), newAuth);
        return newAuth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auth.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auth.remove(authToken);
    }

    @Override
    public void clear() {
        users.clear();
        auth.clear();
        games.clear();
    }

}
