package client;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
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
}