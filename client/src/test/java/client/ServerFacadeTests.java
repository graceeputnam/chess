package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() {
        assertThrows(Exception.class, () -> {
            facade.register("player1", "password", "p1@email.com");
            facade.register("player1", "password", "p1@email.com");
        });
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("player1", "password", "p1@email.com");
        var authData = facade.login("player1", "password");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginNegative() {
        assertThrows(Exception.class, () -> facade.login("nobody", "wrongpassword"));
    }

    @Test
    void logoutPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("badtoken"));
    }

    @Test
    void createGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.createGame(authData.authToken(), "testGame"));
    }

    @Test
    void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("badtoken", "testGame"));
    }

    @Test
    void listGamesPositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        facade.createGame(authData.authToken(), "testGame");
        var games = facade.listGames(authData.authToken());
        assertFalse(games.isEmpty());
    }

    @Test
    void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("badtoken"));
    }

    @Test
    void joinGamePositive() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        var game = facade.createGame(authData.authToken(), "testGame");
        assertDoesNotThrow(() -> facade.joinGame(authData.authToken(), game.gameID(), "WHITE"));
    }

    @Test
    void joinGameNegative() {
        assertThrows(Exception.class, () -> facade.joinGame("badtoken", 9999, "WHITE"));
    }
}