package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class MySqlUserDAO implements DataAccess {

    public MySqlUserDAO() throws
        DataAccessException { configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
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
                    username     VARCHAR(255) NOT NULL PRIMARY KEY,
                    password     VARCHAR(255) NOT NULL,
                    email        VARCHAR(255)
                )
                """;
            stmt.executeUpdate(sql);

        } catch (Exception e) {
            throw new DataAccessException("Unable to configure database", e);
        }
    }

}
