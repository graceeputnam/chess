package websocket.messages;

import chess.ChessGame;
import java.util.Objects;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessageType serverMessageType;
    public ChessGame game;
    public String errorMessage;
    public String message;

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return serverMessageType == that.serverMessageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMessageType);
    }
}