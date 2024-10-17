package dataaccess;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataAccessTests {
    @Test
    public void registerUser() {
        var dataAccess = new MemoryDataAccess();
        var actual = dataAccess.getUser("a");
        var expected = new UserData("a", "p", "j@p.com");
        Assertions.assertEquals(expected, actual);
    }
}
