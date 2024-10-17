package service;

import dataaccess.DataAccess;
import model.*;

import java.util.Objects;

public class Service {
    // write clear() first to pass TA tests
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData registerUser(UserData newUser) throws ServiceException {
        if (dataAccess.getUser(newUser.username()) != null) {
            throw new ServiceException("User already exists!");
        } else {
            UserData user = dataAccess.createUser(newUser);
            dataAccess.createAuth(user.username());
            return user;
        }
    }

    public AuthData login(String username, String password) throws ServiceException {
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new ServiceException("User doesn't exist!");
        } else {
            if (!Objects.equals(user.password(), password)) {
                throw new ServiceException("Password doesn't match!");
            }
            return dataAccess.createAuth(username);
        }
    }


}
