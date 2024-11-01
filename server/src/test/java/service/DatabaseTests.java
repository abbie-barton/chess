package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DatabaseTests {
    static private DataAccess dataAccess;

    @BeforeAll
    public static void init() {
        dataAccess = new MySqlDataAccess();
    }

    @AfterEach
    public void reset() {
        try {
            dataAccess.clear();
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void positiveCreateUser()  {
        UserData user = new UserData("a", "p", "j@p.com");
        UserData result = dataAccess.createUser(user);
        if (result == null) {
            Assertions.fail();
        }
        Assertions.assertEquals(user.username(), result.username());
    }

    @Test
    public void negativeCreateUser() {
        UserData user = new UserData("a", "p", null);
        UserData result = dataAccess.createUser(user);
        Assertions.assertNull(result);
    }

    @Test
    public void positiveGetUser() {
        UserData user = new UserData("austin", "myGardenIsBeautiful", "gardenscapes@scapes.com");
        UserData createResult = dataAccess.createUser(user);
        UserData getResult = dataAccess.getUser(user.username());
        if (getResult == null) {
            Assertions.fail();
        }
        Assertions.assertEquals(getResult.email(), createResult.email());
    }

    @Test
    public void negativeGetUser() {
        UserData user = new UserData("austin", "myGardenIsBeautiful", "gardenscapes@scapes.com");
        UserData createResult = dataAccess.createUser(user);
        UserData getResult = dataAccess.getUser("auztin");
        Assertions.assertNull(getResult);
    }
}
