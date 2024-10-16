package dataaccess;

import model.*;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    final private Map<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String userName) {
        return null;
    }

}
