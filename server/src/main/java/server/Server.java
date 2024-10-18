package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.ServiceException;
import spark.*;
import service.Service;

import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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

        // logout endpoint
        Spark.delete("/session", this::logout);
        Spark.exception(Exception.class, this::exceptionHandler);

        // list games endpoint
        Spark.get("/game", this::listGames);
        Spark.exception(Exception.class, this::exceptionHandler);

        // create game endpoint
        Spark.post("/game", this::createGame);
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

    private String logout(Request req, Response res) throws Exception {
        // where is the authToken coming from? should be in the headers but headers are empty
        AuthData authInfo = serializer.fromJson((Reader) req.headers(), AuthData.class);
        service.logout(authInfo.authToken());
        // what do i do here
        return serializer.toJson(200);
    }

    private String listGames(Request req, Response res) throws Exception {
        AuthData authInfo = serializer.fromJson((Reader) req.headers(), AuthData.class);
        Collection<GameData> result = service.listGames(authInfo.authToken());
        return serializer.toJson(result);
    }

    private String createGame(Request req, Response res) throws Exception {
        AuthData authInfo = serializer.fromJson((Reader) req.headers(), AuthData.class);
        GameData game = serializer.fromJson(req.body(), GameData.class);
        GameData result = service.createGame(authInfo.authToken(), game.gameName());
        return serializer.toJson(result);
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        // handle error codes
        if (ex instanceof ServiceException) {
            if (Objects.equals(ex.getMessage(), "Error: already taken")) {
                res.status(403);
            } else if (Objects.equals(ex.getMessage(), "Error: unauthorized")) {
                res.status(401);
            } else if (Objects.equals(ex.getMessage(), "Error: bad request")) {
                res.status(400);
            }
        } else {
            res.status(500);
        }
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
        ex.printStackTrace(System.out);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
