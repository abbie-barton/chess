package dataaccess;

import model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataAccess {
    UserData getUser(String userName);

    UserData createUser(UserData newUser);

    GameData getGame(int gameID);

    GameData createGame(String gameName);

    Map<String, List<GameData>> listGames();

    void updateGame(int gameID, String playerColor, String username);

    AuthData createAuth(String username);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);

    void clear();
}
