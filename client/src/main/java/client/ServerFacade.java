package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    public void clear() throws Exception {
        makeRequest("/db", "DELETE", null, null);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        var conn = makeRequest("/user", "POST", body, null);
        return readBody(conn, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        var conn = makeRequest("/session", "POST", body, null);
        return readBody(conn, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("/session", "DELETE", null, authToken);
    }

    public GameData createGame(String authToken, String gameName) throws Exception {
        var body = Map.of("gameName", gameName);
        var conn = makeRequest("/game", "POST", body, authToken);
        return readBody(conn, GameData.class);
    }

    public List<GameData> listGames(String authToken) throws Exception {
        var conn = makeRequest("/game", "GET", null, authToken);
        var result = readBody(conn, GamesResponse.class);
        return result.games();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var body = Map.of("gameID", gameID, "playerColor", playerColor);
        makeRequest("/game", "PUT", body, authToken);
    }

    private HttpURLConnection makeRequest(String path, String method, Object body, String authToken) throws Exception {
        var url = URI.create(baseUrl + path).toURL();
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            connection.setRequestProperty("authorization", authToken);
        }
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(gson.toJson(body).getBytes());
            }
        }
        connection.connect();
        if (connection.getResponseCode() >= 400) {
            throw new Exception(readError(connection));
        }
        return connection;
    }

    private String readError(HttpURLConnection connection) throws Exception {
        try (InputStream es = connection.getErrorStream()) {
            if (es != null) {
                var map = gson.fromJson(new InputStreamReader(es), Map.class);
                return (String) map.get("message");
            }
        }
        return "An error occurred";
    }

    private <T> T readBody(HttpURLConnection connection, Class<T> type) throws Exception {
        try (InputStream is = connection.getInputStream()) {
            return gson.fromJson(new InputStreamReader(is), type);
        }
    }

    private record GamesResponse(List<GameData> games) {}
}