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
}
