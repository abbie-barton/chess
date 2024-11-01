package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.util.*;

import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() {
        try {
            configureDatabase();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public UserData getUser(String userName) {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, password, email FROM user WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, userName);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String password = rs.getString("password");
                        String email = rs.getString("email");

                        return new UserData(userName, password, email);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.printf("Unable to read data: %s%n", ex.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public UserData createUser(UserData newUser) {
        UserData user = new UserData(newUser.username(), newUser.password(), newUser.email());
        String hash = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try {
            var id = executeUpdate(statement, newUser.username(), hash, newUser.email());
            System.out.printf("Created user with id %s%n", id);
        } catch (Exception ex) {
            System.out.println("Error creating user");
            return null;
        }
        return user;
    }

    @Override
    public GameData getGame(int gameID) {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, white_username, black_username, game_name, game FROM game WHERE id=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.printf("Unable to read data: %s%n", ex.getMessage());
        }
        return null;
    }

    public GameData readGame(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String whiteUsername = rs.getString("white_username");
        String blackUsername = rs.getString("black_username");
        String gameName = rs.getString("game_name");
        String json = rs.getString("game");
        ChessGame game = new Gson().fromJson(json, ChessGame.class);
        return new GameData(id, whiteUsername, blackUsername, gameName, game);
    }

    @Override
    public GameData createGame(String gameName) {
        var statement = "INSERT INTO game (white_username, black_username, game_name, game) VALUES (?, ?, ?, ?)";
        var json = new Gson().toJson(new ChessGame());
        try {
            var id = executeUpdate(statement, null, null, gameName, json);
            System.out.printf("Created game with id %s%n", id);
        } catch (Exception ex) {
            System.out.println("Error creating game");
            return null;
        }
        return new GameData(1, "", "", gameName, new ChessGame());
    }

    @Override
    public Map<String, List<GameData>> listGames() {
        return new HashMap<>();
    }

    @Override
    public void updateGame(int gameID, String playerColor, String username) {
        System.out.println("Inside updateGame");
    }

    @Override
    public AuthData createAuth(String username) {
        AuthData newAuth = new AuthData(UUID.randomUUID().toString(), username);
        var statement = "INSERT INTO auth (username, auth_token) VALUES (?, ?)";
        try {
            var id = executeUpdate(statement, newAuth.username(), newAuth.authToken());
            System.out.printf("Created auth with id %s%n", id);
        } catch (Exception ex) {
            System.out.println("Error creating auth");
            return null;
        }
        return newAuth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, username FROM auth WHERE auth_token=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String username = rs.getString("username");
                        System.out.printf("Got user with id %s%n", id);

                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.printf("Unable to read data: %s%n", ex.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) {
        var statement = "DELETE FROM auth WHERE auth_token=?";
        try {
            executeUpdate(statement, authToken);
            System.out.println("User logged out successfully");
        } catch (Exception ex) {
            System.out.println("Error logging out user");
        }
    }

    @Override
    public void clear() {
        try {
            var userStatement = "DELETE FROM user";
            executeUpdate(userStatement);
            var authStatement = "DELETE FROM auth";
            executeUpdate(authStatement);
            var gameStatement = "DELETE FROM game";
            executeUpdate(gameStatement);
            System.out.println("Deleted user, auth, game tables");
        } catch (Exception ex) {
            System.out.println("Error clearing database");
        }
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof ChessGame p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(32) NOT NULL,
              `password` varchar(32) NOT NULL,
              `email` varchar(32) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                `id` int NOT NULL AUTO_INCREMENT,
                `auth_token` varchar(256) NOT NULL,
                `username` varchar(32) NOT NULL,
                PRIMARY KEY (`id`),
                INDEX(auth_token)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS game (
                `id` int NOT NULL AUTO_INCREMENT,
                `white_username` varchar(32) NOT NULL,
                `black_username` varchar(32) NOT NULL,
                `game_name` varchar(32) NOT NULL,
                `game` varchar(2048) NOT NULL,
                PRIMARY KEY (`id`),
                INDEX(game_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
