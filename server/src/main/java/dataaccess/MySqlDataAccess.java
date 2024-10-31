package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
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
//        return new UserData("", "", "");
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
        return new GameData(1, "", "", "", new ChessGame());
    }

    @Override
    public GameData createGame(String gameName) {
        return new GameData(1, "", "", "", new ChessGame());
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
        return new AuthData("", "");
    }

    @Override
    public AuthData getAuth(String authToken) {
        return new AuthData("", "");
    }

    @Override
    public void deleteAuth(String authToken) {
        System.out.println("Inside deleteAuth");
    }

    @Override
    public void clear() {
        System.out.println("Inside clear");
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
                `game` varchar(1028) NOT NULL,
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
