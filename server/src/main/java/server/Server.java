package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import spark.*;
import service.Service;

import java.util.Map;

public class Server {
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final Service service = new Service(dataAccess);
    private final Gson serializer = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        // register endpoint
        Spark.post("/user", this::createUser);
        Spark.exception(Exception.class, this::exceptionHandler);

        // login endpoint
        Spark.post("/session", this::login);
        Spark.exception(Exception.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private String createUser(Request req, Response res) throws Exception {
        UserData newUser = serializer.fromJson(req.body(), UserData.class);
        UserData result = service.registerUser(newUser);
        return serializer.toJson(result);
    }

    private String login(Request req, Response res) throws Exception {
        UserData userInfo = serializer.fromJson(req.body(), UserData.class);
        AuthData result = service.login(userInfo.username(), userInfo.password());
        return serializer.toJson(result);
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        // handle other error codes
        res.status(500);
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
        ex.printStackTrace(System.out);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
