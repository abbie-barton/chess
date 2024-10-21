package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ServiceTests {
    static private DataAccess dataAccess;
    static private Service service;

    @BeforeAll
    public static void init() {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }

    @Test
    public void positiveRegisterUser()  {
        try {
            UserData user = new UserData("a", "p", "j@p.com");
            AuthData registrationResult = service.registerUser(user);
            Assertions.assertEquals(user.username(), registrationResult.username());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeRegisterUser() {
        try {
            UserData badUser = new UserData("", null, null);
            service.registerUser(badUser);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: bad request");
        }
    }

    @Test
    public void positiveLogin() {
        try {
            UserData user = new UserData("a", "abc", "a@gmail.com");
            AuthData registerResult = service.registerUser(user);
            AuthData loginResult = service.login(user.username(), user.password());
            Assertions.assertNotNull(dataAccess.getAuth(loginResult.authToken()));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogin() {
        try {
            UserData user = new UserData("a", "abc", "a@gmail.com");
            AuthData loginResult = service.login(user.username(), user.password());
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: unauthorized");
        }
    }

    @Test
    public void positiveLogout() {
        try {
            UserData user = new UserData("yoyo", "yoyorocks", "yoyo@hotmail.com");
            AuthData registerResult = service.registerUser(user);
            service.logout(registerResult.authToken());
            Assertions.assertNull(dataAccess.getAuth(registerResult.authToken()));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeLogout() {
        try {
            String fakeAuth = UUID.randomUUID().toString();
            service.logout(fakeAuth);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), "Error: unauthorized");
        }
    }
}
