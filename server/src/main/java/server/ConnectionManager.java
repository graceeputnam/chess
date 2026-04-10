package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, Map<String, Session>> gameSessions = new ConcurrentHashMap<>();

    public void add(int gameID, String username, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(username, session);
    }

    public void remove(int gameID, String username) {
        Map<String, Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(username);
        }
    }

    public void sendToOne(int gameID, String username, String message) throws IOException {
        Map<String, Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            Session session = sessions.get(username);
            if (session != null && session.isOpen()) {
                session.getRemote().sendString(message);
            }
        }
    }

    public void broadcast(int gameID, String excludeUsername, String message) throws IOException {
        Map<String, Session> sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return;
        }
        for (var entry : sessions.entrySet()) {
            if (!entry.getKey().equals(excludeUsername) && entry.getValue().isOpen()) {
                entry.getValue().getRemote().sendString(message);
            }
        }
    }
}