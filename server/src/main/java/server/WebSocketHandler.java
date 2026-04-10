package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import chess.InvalidMoveException;

import java.io.IOException;

public class WebSocketHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onMessage(io.javalin.websocket.WsMessageContext ctx) throws IOException {
        Session session = ctx.session;
        String message = ctx.message();
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(session, command);
            case MAKE_MOVE -> handleMakeMove(session, gson.fromJson(message, MakeMoveCommand.class));
            case LEAVE -> handleLeave(session, command);
            case RESIGN -> handleResign(session, command);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        var auth = getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData game = getGame(session, command.getGameID());
        if (game == null) {
            return;
        }

        String username = auth.username();
        connections.add(command.getGameID(), username, session);

        var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.game = game.game();
        sendToOne(session, loadMessage);

        String role = getRole(game, username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.message = username + " joined the game as " + role;
        connections.broadcast(command.getGameID(), username, gson.toJson(notification));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) throws IOException {
        var auth = getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData gameData = getGame(session, command.getGameID());
        if (gameData == null) {
            return;
        }

        String username = auth.username();
        ChessGame game = gameData.game();

        if (game.isOver()) {
            sendError(session, "Error: game is over");
            return;
        }

        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            sendError(session, "Error: observers cannot make moves");
            return;
        }

        if (isWhite && game.getTeamTurn() != ChessGame.TeamColor.WHITE) {
            sendError(session, "Error: it is not your turn");
            return;
        }

        if (isBlack && game.getTeamTurn() != ChessGame.TeamColor.BLACK) {
            sendError(session, "Error: it is not your turn");
            return;
        }

        try {
            game.makeMove(command.move);
        } catch (InvalidMoveException e) {
            sendError(session, "Error: invalid move");
            return;
        }

        boolean isCheckmate = game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                game.isInCheckmate(ChessGame.TeamColor.BLACK);
        boolean isStalemate = game.isInStalemate(ChessGame.TeamColor.WHITE) ||
                game.isInStalemate(ChessGame.TeamColor.BLACK);

        if (isCheckmate || isStalemate) {
            game.setOver(true);
        }

        try {
            dataAccess.updateGame(new GameData(gameData.gameID(), gameData.gameName(),
                    gameData.whiteUsername(), gameData.blackUsername(), game));
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
            return;
        }

        var loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadMessage.game = game;
        String loadJson = gson.toJson(loadMessage);
        sendToOne(session, loadMessage);
        connections.broadcast(command.getGameID(), username, loadJson);

        var moveNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        moveNotification.message = username + " moved " + command.move.toString();
        connections.broadcast(command.getGameID(), username, gson.toJson(moveNotification));

        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            sendNotificationToAll(command.getGameID(), gameData.whiteUsername() + " is in checkmate!");
        } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            sendNotificationToAll(command.getGameID(), gameData.blackUsername() + " is in checkmate!");
        } else if (isStalemate) {
            sendNotificationToAll(command.getGameID(), "Stalemate! The game is over.");
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            sendNotificationToAll(command.getGameID(), gameData.whiteUsername() + " is in check!");
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            sendNotificationToAll(command.getGameID(), gameData.blackUsername() + " is in check!");
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws IOException {
        var auth = getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData gameData = getGame(session, command.getGameID());
        if (gameData == null) {
            return;
        }

        String username = auth.username();

        if (username.equals(gameData.whiteUsername())) {
            try {
                dataAccess.updateGame(new GameData(gameData.gameID(), gameData.gameName(),
                        null, gameData.blackUsername(), gameData.game()));
            } catch (DataAccessException e) {
                sendError(session, "Error: " + e.getMessage());
                return;
            }
        } else if (username.equals(gameData.blackUsername())) {
            try {
                dataAccess.updateGame(new GameData(gameData.gameID(), gameData.gameName(),
                        gameData.whiteUsername(), null, gameData.game()));
            } catch (DataAccessException e) {
                sendError(session, "Error: " + e.getMessage());
                return;
            }
        }

        connections.remove(command.getGameID(), username);

        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.message = username + " left the game";
        connections.broadcast(command.getGameID(), username, gson.toJson(notification));
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {
        var auth = getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData gameData = getGame(session, command.getGameID());
        if (gameData == null) {
            return;
        }

        String username = auth.username();
        ChessGame game = gameData.game();

        boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());
        if (!isPlayer) {
            sendError(session, "Error: observers cannot resign");
            return;
        }

        if (game.isOver()) {
            sendError(session, "Error: game is already over");
            return;
        }

        game.setOver(true);
        try {
            dataAccess.updateGame(new GameData(gameData.gameID(), gameData.gameName(),
                    gameData.whiteUsername(), gameData.blackUsername(), game));
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
            return;
        }

        sendNotificationToAll(command.getGameID(), username + " resigned. The game is over.");
    }

    private void sendNotificationToAll(int gameID, String message) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.message = message;
        connections.broadcast(gameID, "", gson.toJson(notification));
    }

    private String getRole(GameData game, String username) {
        if (username.equals(game.whiteUsername())) {
            return "white";
        }
        if (username.equals(game.blackUsername())) {
            return "black";
        }
        return "an observer";
    }

    private model.AuthData getAuth(String authToken) {
        try {
            return dataAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private GameData getGame(Session session, int gameID) throws IOException {
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                sendError(session, "Error: game not found");
                return null;
            }
            return game;
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
            return null;
        }
    }

    private void sendToOne(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        if (session.isOpen()) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            error.errorMessage = errorMessage;
            session.getRemote().sendString(gson.toJson(error));
        }
    }
}