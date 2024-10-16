package service;

import dataaccess.DataAccess;
import model.*;

public class Service {
    // write clear() first to pass TA tests
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData registerUser(UserData newUser) throws ServiceException {
        if (dataAccess.getUser(newUser.username()) != null) {
            throw new ServiceException("User already exists")
        }
        return newUser;
    }

}
