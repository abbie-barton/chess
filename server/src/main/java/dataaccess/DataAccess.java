package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    UserData getUser(String userName);

    UserData createUser(UserData newUser);

    GameData getGame(String gameID);

    GameData createGame(String gameName);

    Collection<GameData> listGames();

    void updateGame(String gameID, String playerColor, String username);

    AuthData createAuth(String username);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);

    void clear();
}
