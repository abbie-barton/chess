package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:8080");
    }

    @AfterEach
    public void reset() {
        try {
            serverFacade.clear();
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void positiveCreateUser() {
        UserData newUser = new UserData("a", "a", "a");
        try {
            AuthData returnedAuth = serverFacade.createUser(newUser);
            Assertions.assertNotNull(returnedAuth);
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void negativeCreateUser() {
        UserData newUser = new UserData(null, null, null);
        try {
            AuthData returnedAuth = serverFacade.createUser(newUser);
            Assertions.fail();
        } catch (Exception ex) {
            Assertions.assertNotNull(ex);
        }
    }

}
