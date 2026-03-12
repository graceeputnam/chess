package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class MySqlUserDAO implements DataAccess {

    public MySqlUserDAO() {
        // later: call helper to configure DB / tables using DatabaseManager
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
}
