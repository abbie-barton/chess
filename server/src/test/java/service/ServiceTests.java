package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ServiceTests {
    static private DataAccess dataAccess;
    static private Service service;

    @BeforeAll
    public static void init() {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }
    @Test
    public void registerUser() throws Exception {
        var user = new UserData("a", "p", "j@p.com");
        var registrationResult = service.registerUser(user);
        Assertions.assertEquals(user, registrationResult);
    }
}
