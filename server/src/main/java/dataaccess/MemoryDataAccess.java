package dataaccess;

import chess.ChessGame;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private int nextGameId = 1;
    final private Map<String, UserData> users = new HashMap<>();
    final private Map<String, AuthData> auth = new HashMap<>();
    final private Map<Integer, ModifiedGameData> games = new HashMap<>();

    @Override
    public UserData getUser(String userName) {
        return users.get(userName);
    }

    @Override
    public UserData createUser(UserData newUser) {
        String hash = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        UserData user = new UserData(newUser.username(), hash, newUser.email());

        users.put(newUser.username(), user);
        return user;
    }

    @Override
    public ModifiedGameData getGame(int gameID) {
        ModifiedGameData game = games.get(gameID);
        return new ModifiedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game(), 0);
    }

    @Override
    public GameData createGame(String gameName) {
        ModifiedGameData game = new ModifiedGameData(nextGameId, null, null,
                gameName, new ChessGame(), 0);
        GameData returnGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game());
        games.put(nextGameId, game);
        nextGameId++;
        return returnGame;
    }

    @Override
    public Map<String, List<GameData>> listGames() {
        List<GameData> gameList = new ArrayList<>();
        for (ModifiedGameData game : games.values()) {
            GameData newGame = new GameData(game.gameID(), game.whiteUsername(),
                    game.blackUsername(), game.gameName(), game.game());
            gameList.add(newGame);
        }
        return Map.of("games", gameList);
    }

    @Override
    public void updateGame(int gameID, String playerColor, String username) {
        // update game
        ModifiedGameData game = games.get(gameID);
        if (Objects.equals(playerColor, "WHITE")) {
            games.put(gameID, new ModifiedGameData(gameID, username, game.blackUsername(), game.gameName(), game.game(), 0));
        } else {
            games.put(gameID, new ModifiedGameData(gameID, game.whiteUsername(), username, game.gameName(), game.game(), 0));
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
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            auth.remove(authToken);
        } catch (Exception ex) {
            throw new DataAccessException("Error: bad request");
        }
    }

    @Override
    public void markGameAsOver(int gameID) {
        ModifiedGameData game = games.get(gameID);
        ModifiedGameData completedGame = new ModifiedGameData(game.gameID(), game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game(), 1);
        games.put(gameID, completedGame);
    }

    @Override
    public void updateGameMoves(int gameID, ChessGame newGame) {
        ModifiedGameData game = games.get(gameID);
        ModifiedGameData updatedGame = new ModifiedGameData(game.gameID(), game.whiteUsername(),
                game.blackUsername(), game.gameName(), newGame, game.is_over());
        games.put(gameID, updatedGame);
    }

    @Override
    public void clear() {
        users.clear();
        auth.clear();
        games.clear();
    }

}
