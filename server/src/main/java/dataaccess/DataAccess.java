package dataaccess;

import model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataAccess {
    UserData getUser(String userName);

    UserData createUser(UserData newUser);

    ModifiedGameData getGame(int gameID);

    GameData createGame(String gameName);

    Map<String, List<GameData>> listGames();

    void updateGame(int gameID, String playerColor, String username);

    AuthData createAuth(String username);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken) throws DataAccessException;

    void clear();

    void markGameAsOver(int gameID);
}
