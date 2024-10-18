package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import model.*;

import java.util.Collection;
import java.util.Objects;

public class Service {
    // write clear() first to pass TA tests
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData registerUser(UserData newUser) throws ServiceException {
        if (newUser.username() == null || newUser.password() == null || newUser.email() == null) {
            throw new ServiceException("Error: bad request");
        }
        if (dataAccess.getUser(newUser.username()) != null) {
            throw new ServiceException("Error: already taken");
        } else {
            UserData user = dataAccess.createUser(newUser);
            dataAccess.createAuth(user.username());
            return user;
        }
    }

    public AuthData login(String username, String password) throws ServiceException {
        if (username == null || password == null) {
            throw new ServiceException("Error: bad request");
        }
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new ServiceException("Error: User doesn't exist!");
        } else {
            if (!Objects.equals(user.password(), password)) {
                throw new ServiceException("Error: Password doesn't match!");
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

    public Collection<GameData> listGames(String authToken) throws ServiceException {
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
}
