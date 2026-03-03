package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private DataAccess dataAccess;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
    }

    // register: positive
    @Test
    public void registerSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password", "a@mail.com");
        AuthResult result = userService.register(user);
        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
    }

    // register: negative
    @Test
    public void registerBadRequest() {
        UserData badUser = new UserData("bob", null, "b@mail.com");
        assertThrows(BadRequestException.class, () -> userService.register(badUser));
    }

    // login: positive
    @Test
    public void loginSuccess() throws DataAccessException {
        UserData user = new UserData("carol", "pw", "c@mail.com");
        userService.register(user);
        UserData loginUser = new UserData("carol", "pw", null);
        AuthResult result = userService.login(loginUser);
        assertEquals("carol", result.username());
        assertNotNull(result.authToken());
    }

    // login: negative
    @Test
    public void loginUnauthorized() throws DataAccessException {
        UserData user = new UserData("dave", "right", "d@mail.com");
        userService.register(user);
        UserData wrongUser = new UserData("dave", "wrong", null);
        assertThrows(UnauthorizedException.class, () -> userService.login(wrongUser));
    }

    // logout: positive
    @Test
    public void logoutSuccess() throws DataAccessException {
        UserData user = new UserData("erin", "pw", "e@mail.com");
        AuthResult result = userService.register(user);
        assertDoesNotThrow(() -> userService.logout(result.authToken()));
    }

    // logout: negative
    @Test
    public void logoutUnauthorized() {
        String fakeToken = "not-real-token";
        assertThrows(UnauthorizedException.class, () -> userService.logout(fakeToken));
    }
}
