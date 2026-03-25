package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.GameData;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    public void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    private String getAuthToken() throws DataAccessException {
        UserData user = new UserData("player", "pw", "p@mail.com");
        userService.register(user);
        AuthResult loginResult = userService.login(new UserData("player", "pw", null));
        return loginResult.authToken();
    }

    // clear: positive
    @Test
    public void clearSuccess() throws DataAccessException {
        String token = getAuthToken();
        gameService.createGame(token, new CreateGameRequest("test game"));

        gameService.clear();

        assertTrue(dataAccess.listGames().isEmpty());
    }

    // listGames: positive
    @Test
    public void listGamesSuccess() throws DataAccessException {
        String token = getAuthToken();
        gameService.createGame(token, new CreateGameRequest("game1"));
        gameService.createGame(token, new CreateGameRequest("game2"));

        ListGamesResult result = gameService.listGames(token);

        assertNotNull(result.games());
        assertEquals(2, result.games().length);
    }

    // listGames: negative
    @Test
    public void listGamesUnauthorized() {
        String badToken = "bad";

        assertThrows(UnauthorizedException.class, () -> gameService.listGames(badToken));
    }

    // createGame: positive
    @Test
    public void createGameSuccess() throws DataAccessException {
        String token = getAuthToken();

        CreateGameResult result = gameService.createGame(token, new CreateGameRequest("my game"));

        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);
    }

    // createGame: negative
    @Test
    public void createGameBadRequest() throws DataAccessException {
        String token = getAuthToken();
        CreateGameRequest badRequest = new CreateGameRequest(null);

        assertThrows(BadRequestException.class, () -> gameService.createGame(token, badRequest));
    }

    // joinGame: positive
    @Test
    public void joinGameSuccess() throws DataAccessException {
        String token = getAuthToken();
        CreateGameResult createResult = gameService.createGame(token, new CreateGameRequest("joinable"));
        Integer gameID = createResult.gameID();

        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", gameID);
        gameService.joinGame(token, joinRequest);

        GameData game = dataAccess.getGame(gameID);
        assertEquals("player", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    // joinGame: negative
    @Test
    public void joinGameBadColor() throws DataAccessException {
        String token = getAuthToken();
        CreateGameResult createResult = gameService.createGame(token, new CreateGameRequest("joinable"));
        Integer gameID = createResult.gameID();

        JoinGameRequest joinRequest = new JoinGameRequest("GREEN", gameID);

        assertThrows(BadRequestException.class, () -> gameService.joinGame(token, joinRequest));
    }
}
