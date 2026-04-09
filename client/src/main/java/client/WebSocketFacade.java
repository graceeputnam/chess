package client;

import chess.ChessMove;
import com.google.gson.Gson;
import org.glassfish.tyrus.client.ClientManager;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {

    private Session session;
    private final Gson gson = new Gson();
    private final MessageHandler messageHandler;

    public interface MessageHandler {
        void onMessage(ServerMessage message);
    }

    public WebSocketFacade(int port, MessageHandler messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        URI uri = new URI("ws://localhost:" + port + "/ws");
        ClientManager client = ClientManager.createClient();
        client.connectToServer(this, uri);
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            messageHandler.onMessage(serverMessage);
        } catch (Exception e) {
            System.out.println("Error handling message: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    public void sendConnect(String authToken, int gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void sendMakeMove(String authToken, int gameID, ChessMove move) throws Exception {
        var command = new MakeMoveCommand(authToken, gameID, move);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void sendLeave(String authToken, int gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void sendResign(String authToken, int gameID) throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}