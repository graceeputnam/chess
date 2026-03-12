package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

}
