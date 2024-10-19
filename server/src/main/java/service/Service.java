package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Service {
    // write clear() first to pass TA tests
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData registerUser(UserData newUser) throws ServiceException {
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new ServiceException("Error: bad request");
        }
        if (dataAccess.getUser(newUser.username()) != null) {
            throw new ServiceException("Error: already taken");
        } else {
            UserData user = dataAccess.createUser(newUser);
            return dataAccess.createAuth(user.username());
        }
    }

    public AuthData login(String username, String password) throws ServiceException {
        if (username == null || password == null) {
            throw new ServiceException("Error: bad request");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            if (!Objects.equals(user.password(), password)) {
                throw new ServiceException("Error: unauthorized");
            }
            return dataAccess.createAuth(username);
        }
    }

    public void logout(String authToken) throws ServiceException {
        if (authToken == null) {
            throw new ServiceException("Error: bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            dataAccess.deleteAuth(authToken);
        }
    }

    public Map<String, List<GameData>> listGames(String authToken) throws ServiceException {
        if (authToken == null) {
            throw new ServiceException("Error: bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            return dataAccess.listGames();
        }
    }

    public GameData createGame(String authToken, String gameName) throws ServiceException {
        if (gameName == null) {
            throw new ServiceException("Error: bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            return dataAccess.createGame(gameName);
        }
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ServiceException {
        if (authToken == null || playerColor == null || gameID <= 0 ) {
            throw new ServiceException("Error: bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new ServiceException("Error: unauthorized");
        } else {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException("Error: bad request");
            }
            if (game.blackUsername() != null && game.whiteUsername() != null) {
                throw new ServiceException("Error: already taken");
            }
            dataAccess.updateGame(gameID, playerColor, auth.username());
        }
    }

    public void clear() throws ServiceException {
        dataAccess.clear();
    }
}
