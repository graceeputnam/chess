package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;

public class MySqlUserDAO implements DataAccess {

    public MySqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            var dbName = "chess";
            stmt.executeUpdate("USE " + dbName);
            stmt.executeUpdate("TRUNCATE TABLE user");

        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear user table", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) {
            throw new DataAccessException("invalid user");
        }

        var sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {

            var dbName = "chess";
            try (var useStmt = conn.createStatement()) {
                useStmt.executeUpdate("USE " + dbName);
            }

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.username());
                stmt.setString(2, user.password());
                stmt.setString(3, user.email());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM user WHERE username = ?";

        try (var conn = DatabaseManager.getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var name = rs.getString("username");
                    var password = rs.getString("password");
                    var email = rs.getString("email");
                    return new UserData(name, password, email);
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Unable to get user", e);
        }
    }


    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    private void configureDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            var dbName = "chess";

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            stmt.executeUpdate("USE " + dbName);

            var sql = """
                    CREATE TABLE IF NOT EXISTS user (
                        username  VARCHAR(255) NOT NULL PRIMARY KEY,
                        password  VARCHAR(255) NOT NULL,
                        email     VARCHAR(255)
                    )
                    """;
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database", e);
        }
    }
}
