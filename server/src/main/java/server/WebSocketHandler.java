package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final DataAccess dataAccess;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(session, command);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        var auth = getAuth(command.getAuthToken());
        if (auth == null) {
            sendError(session, "Error: unauthorized");
            return;
        }

        GameData game = getGame(session, command.getGameID());
        if (game == null) return;

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

    private String getRole(GameData game, String username) {
        if (username.equals(game.whiteUsername())) return "white";
        if (username.equals(game.blackUsername())) return "black";
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
        session.getRemote().sendString(gson.toJson(message));
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        error.errorMessage = errorMessage;
        session.getRemote().sendString(gson.toJson(error));
    }
}