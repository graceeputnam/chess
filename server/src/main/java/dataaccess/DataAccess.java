package dataaccess;

import java.util.Collection;
import model.UserData;
import model.GameData;
import model.AuthData;

public interface DataAccess {
    void clear() throws DataAccessException;

    //user methods
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    //auth methods
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    //game methods
    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}
