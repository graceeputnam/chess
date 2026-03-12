package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MySqlUserDAO implements DataAccess {

    private final Gson gson = new Gson();

    public MySqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE auth");
            stmt.executeUpdate("TRUNCATE TABLE game");
            stmt.executeUpdate("TRUNCATE TABLE user");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear tables", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) {
            throw new DataAccessException("invalid user");
        }

        var sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new service.ForbiddenException("already taken");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM user WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var name = rs.getString("username");
                    var password = rs.getString("password");
                    var email = rs.getString("email");
                    return new UserData(name, password, email);
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Unable to get user", e);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        var user = getUser(username);
        if (user == null) {
            throw new DataAccessException("user doesn't exist");
        }

        var token = UUID.randomUUID().toString();
        var sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();

            return new AuthData(token, username);

        } catch (Exception e) {
            throw new DataAccessException("Unable to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql = "SELECT authToken, username FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var token = rs.getString("authToken");
                    var username = rs.getString("username");
                    return new AuthData(token, username);
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Unable to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Unable to delete auth", e);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("invalid game");
        }

        var sql = """
                INSERT INTO game (gameName, whiteUsername, blackUsername, game)
                VALUES (?, ?, ?, ?)
                """;

        var name = game.gameName() == null ? "" : game.gameName();
        var json = gson.toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, json);
            stmt.executeUpdate();

            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt("GENERATED_KEY");
                } else {
                    throw new DataAccessException("Unable to get generated game ID");
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Unable to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = """
                SELECT gameID, gameName, whiteUsername, blackUsername, game
                FROM game
                WHERE gameID = ?
                """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var id = rs.getInt("gameID");
                    var name = rs.getString("gameName");
                    var white = rs.getString("whiteUsername");
                    var black = rs.getString("blackUsername");
                    var json = rs.getString("game");
                    var chessGame = gson.fromJson(json, ChessGame.class);

                    return new GameData(id, name, white, black, chessGame);
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Unable to get game", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var sql = """
                SELECT gameID, gameName, whiteUsername, blackUsername, game
                FROM game
                """;

        var games = new ArrayList<GameData>();

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                var id = rs.getInt("gameID");
                var name = rs.getString("gameName");
                var white = rs.getString("whiteUsername");
                var black = rs.getString("blackUsername");
                var json = rs.getString("game");
                var chessGame = gson.fromJson(json, ChessGame.class);

                games.add(new GameData(id, name, white, black, chessGame));
            }

            return games;

        } catch (Exception e) {
            throw new DataAccessException("Unable to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("invalid game");
        }

        var sql = """
                UPDATE game
                SET gameName = ?, whiteUsername = ?, blackUsername = ?, game = ?
                WHERE gameID = ?
                """;

        var name = game.gameName() == null ? "" : game.gameName();
        var json = gson.toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);                 // gameName
            stmt.setString(2, game.whiteUsername()); // whiteUsername
            stmt.setString(3, game.blackUsername()); // blackUsername
            stmt.setString(4, json);                 // game (JSON)
            stmt.setInt(5, game.gameID());           // WHERE gameID = ?
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            var userSql = """
                CREATE TABLE IF NOT EXISTS user (
                    username  VARCHAR(255) NOT NULL PRIMARY KEY,
                    password  VARCHAR(255) NOT NULL,
                    email     VARCHAR(255)
                )
                """;
            stmt.executeUpdate(userSql);

            var authSql = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken  VARCHAR(255) NOT NULL PRIMARY KEY,
                    username   VARCHAR(255) NOT NULL
                )
                """;
            stmt.executeUpdate(authSql);

            var gameSql = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID        INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    gameName      VARCHAR(255) NOT NULL,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    game          TEXT NOT NULL
                )
                """;
            stmt.executeUpdate(gameSql);

        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database", e);
        }
    }
}
