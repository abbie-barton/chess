package dataaccess;

import model.*;

public interface DataAccess {
    UserData getUser(String userName);

    UserData createUser(String userName, String password, String email);

    GameData getGame(String gameID);

    GameData createGame(String gameName);

    Collection<GameData> listGames();

    void updateGame(String gameID, String playerColor, String username);

    AuthData createAuth(String username);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);

    void clear();
}
