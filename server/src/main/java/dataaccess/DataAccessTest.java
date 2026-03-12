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
        dao = new MySqlUserDAO();   // initializes DB + tables
        dao.clear();                // start each test with empty DB
    }

    @Test
    void createUser_positive() throws DataAccessException {
        var user = new UserData("jane", "password", "jane@example.com");
        dao.createUser(user);
        var fromDb = dao.getUser("jane");
        assertNotNull(fromDb);
        assertEquals("jane", fromDb.username());
    }

    @Test
    void createUser_negative_duplicateUsername() throws DataAccessException {
        var user1 = new UserData("alice", "password", "alice@example.com");
        var user2 = new UserData("alice", "different", "other@example.com");
        dao.createUser(user1);
        assertThrows(DataAccessException.class, () -> dao.createUser(user2));
    }

    @Test
    void createAuth_positive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");

        assertNotNull(auth);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void createAuth_negative_unknownUser() {
        assertThrows(DataAccessException.class, () -> dao.createAuth("no-such-user"));
    }

    @Test
    void getAuth_positive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");

        var fromDb = dao.getAuth(auth.authToken());
        assertNotNull(fromDb);
        assertEquals(auth.authToken(), fromDb.authToken());
        assertEquals("alice", fromDb.username());
    }

    @Test
    void getAuth_negative_notFound() throws DataAccessException {
        var fromDb = dao.getAuth("no-such-token");
        assertNull(fromDb);
    }

    @Test
    void deleteAuth_positive() throws DataAccessException {
        var user = new UserData("alice", "password", "alice@example.com");
        dao.createUser(user);
        var auth = dao.createAuth("alice");

        dao.deleteAuth(auth.authToken());
        var fromDb = dao.getAuth(auth.authToken());
        assertNull(fromDb);
    }

    @Test
    void deleteAuth_negative_notFound() throws DataAccessException {
        dao.deleteAuth("no-such-token");
    }

    @Test
    void createGame_positive() throws DataAccessException {
        var chessGame = new ChessGame();
        var game = new GameData(0, "cool game", null, null, chessGame);

        var id = dao.createGame(game);
        assertTrue(id > 0);

        var fromDb = dao.getGame(id);
        assertNotNull(fromDb);
        assertEquals("cool game", fromDb.gameName());
        assertNotNull(fromDb.game());
    }

    @Test
    void createGame_negative_nullGame() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    void getGame_negative_notFound() throws DataAccessException {
        var fromDb = dao.getGame(999999);
        assertNull(fromDb);
    }

    @Test
    void listGames_positive_multiple() throws DataAccessException {
        var game1 = new GameData(0, "g1", null, null, new ChessGame());
        var game2 = new GameData(0, "g2", "white", "black", new ChessGame());
        var id1 = dao.createGame(game1);
        var id2 = dao.createGame(game2);
        var games = dao.listGames();
        var ids = games.stream().map(GameData::gameID).collect(Collectors.toSet());

        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }

    @Test
    void updateGame_positive() throws DataAccessException {
        var chessGame = new ChessGame();
        var game = new GameData(0, "orig", null, null, chessGame);
        var id = dao.createGame(game);

        var updated = new GameData(id, "updated", "whitePlayer", "blackPlayer", chessGame);
        dao.updateGame(updated);

        var fromDb = dao.getGame(id);
        assertNotNull(fromDb);
        assertEquals("updated", fromDb.gameName());
        assertEquals("whitePlayer", fromDb.whiteUsername());
        assertEquals("blackPlayer", fromDb.blackUsername());
    }

    @Test
    void updateGame_negative_null() {
        assertThrows(DataAccessException.class, () -> dao.updateGame(null));
    }
}
