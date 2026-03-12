package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess dao;

    @BeforeEach
    void setUp() throws DataAccessException {
        dao = new MySqlUserDAO();
        dao.clear();
    }

    @Test
    void createUserPositive() throws DataAccessException {
        var user = new UserData("jane", "password", "jane@example.com");
        dao.createUser(user);
        var fromDb = dao.getUser("jane");
        assertNotNull(fromDb);
        assertEquals("jane", fromDb.username());
    }

    @Test
    void createUserNegativeDuplicate() throws DataAccessException {
        var user1 = new UserData("alice", "password", "alice@example.com");
        var user2 = new UserData("alice", "different", "other@example.com");
        dao.createUser(user1);
        assertThrows(service.ForbiddenException.class, () -> dao.createUser(user2));
    }

    @Test
    void getUserPositive() throws DataAccessException {
        var user = new UserData("bob", "pass", "bob@example.com");
        dao.createUser(user);
        var fromDb = dao.getUser("bob");
        assertNotNull(fromDb);
        assertEquals("bob", fromDb.username());
    }

    @Test
    void getUserNegativeNotFound() throws DataAccessException {
        var fromDb = dao.getUser("nobody");
        assertNull(fromDb);
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");
        assertNotNull(auth);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void createAuthNegativeUnknownUser() {
        assertThrows(DataAccessException.class, () -> dao.createAuth("no-such-user"));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");
        var fromDb = dao.getAuth(auth.authToken());
        assertNotNull(fromDb);
        assertEquals(auth.authToken(), fromDb.authToken());
        assertEquals("alice", fromDb.username());
    }

    @Test
    void getAuthNegativeNotFound() throws DataAccessException {
        var fromDb = dao.getAuth("no-such-token");
        assertNull(fromDb);
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");
        dao.deleteAuth(auth.authToken());
        var fromDb = dao.getAuth(auth.authToken());
        assertNull(fromDb);
    }

    @Test
    void deleteAuthNegativeNotFound() throws DataAccessException {
        // deleting a non-existent token should not throw
        assertDoesNotThrow(() -> dao.deleteAuth("no-such-token"));
    }

    @Test
    void createGamePositive() throws DataAccessException {
        var game = new GameData(0, "cool game", null, null, new ChessGame());
        var id = dao.createGame(game);
        assertTrue(id > 0);
        var fromDb = dao.getGame(id);
        assertNotNull(fromDb);
        assertEquals("cool game", fromDb.gameName());
    }

    @Test
    void createGameNegativeNullGame() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        var game = new GameData(0, "test game", null, null, new ChessGame());
        var id = dao.createGame(game);
        var fromDb = dao.getGame(id);
        assertNotNull(fromDb);
        assertEquals("test game", fromDb.gameName());
    }

    @Test
    void getGameNegativeNotFound() throws DataAccessException {
        var fromDb = dao.getGame(999999);
        assertNull(fromDb);
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        var id1 = dao.createGame(new GameData(0, "g1", null, null, new ChessGame()));
        var id2 = dao.createGame(new GameData(0, "g2", null, null, new ChessGame()));
        var games = dao.listGames();
        var ids = games.stream().map(GameData::gameID).collect(Collectors.toSet());
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }

    @Test
    void listGamesNegativeEmpty() throws DataAccessException {
        var games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        var id = dao.createGame(new GameData(0, "orig", null, null, new ChessGame()));
        var updated = new GameData(id, "updated", "white", "black", new ChessGame());
        dao.updateGame(updated);
        var fromDb = dao.getGame(id);
        assertNotNull(fromDb);
        assertEquals("updated", fromDb.gameName());
        assertEquals("white", fromDb.whiteUsername());
    }

    @Test
    void updateGameNegativeNull() {
        assertThrows(DataAccessException.class, () -> dao.updateGame(null));
    }
}