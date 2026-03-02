package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.ArrayList;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    private String checkAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }
        return auth.username();
    }

    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        checkAuth(authToken);
        Collection<GameData> gameCollection = dataAccess.listGames();
        ArrayList<GameData> gameList = new ArrayList<>(gameCollection);
        GameData[] gamesArray = gameList.toArray(new GameData[0]);

        return new ListGamesResult(gamesArray, null);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws DataAccessException {
        String username = checkAuth(authToken);
        if (request == null || request.gameName() == null) {
            throw new BadRequestException("bad request");
        }

        String gameName = request.gameName();
        ChessGame chessGame = new ChessGame();

        GameData newGame = new GameData(
                0,
                null,
                null,
                gameName,
                chessGame);

        int id = dataAccess.createGame(newGame);
        return new CreateGameResult(id, null);
    }

    public void joinGame(String authToken, JoinGameRequest request)
            throws DataAccessException {
        String username = checkAuth(authToken);
        if (request == null || request.gameID() == null || request.playerColor() == null) {
            throw new BadRequestException("bad request");
        }

        Integer gameID = request.gameID();
        String playerColor = request.playerColor();
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if ("WHITE".equals(playerColor)) {
            if (whiteUsername != null) {
                throw new ForbiddenException("already taken");
            }
            whiteUsername = username;
        } else if ("BLACK".equals(playerColor)) {
            if (blackUsername != null) {
                throw new ForbiddenException("already taken");
            }
            blackUsername = username;
        } else {
            throw new BadRequestException("bad request");
        }

        GameData updated = new GameData(
                game.gameID(),
                whiteUsername,
                blackUsername,
                game.gameName(),
                game.game());

        dataAccess.updateGame(updated);
    }
}
