package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlUserDAO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import model.UserData;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        try {
            this.dataAccess = new MySqlUserDAO(); // initializes DB + tables
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);
    }

    private void handleClear(Context ctx) {
        try {
            gameService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleRegister(Context ctx) {
        try {
            UserData request = gson.fromJson(ctx.body(), UserData.class);
            AuthResult result = userService.register(request);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(new ErrorResult("Error: bad request")));
        } catch (ForbiddenException e) {
            ctx.status(403);
            ctx.result(gson.toJson(new ErrorResult("Error: already taken")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleLogin(Context ctx) {
        try {
            UserData request = gson.fromJson(ctx.body(), UserData.class);
            UserData loginRequest = new UserData(request.username(), request.password(), null);

            AuthResult result = userService.login(loginRequest);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(new ErrorResult("Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(new ErrorResult("Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleLogout(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            userService.logout(authToken);
            ctx.status(200);
            ctx.result("{}");
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(new ErrorResult("Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleListGames(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            ListGamesResult result = gameService.listGames(authToken);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(new ErrorResult("Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleCreateGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(authToken, request);
            ctx.status(200);
            ctx.result(gson.toJson(result));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(new ErrorResult("Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(new ErrorResult("Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    private void handleJoinGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, request);
            ctx.status(200);
            ctx.result("{}");
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(gson.toJson(new ErrorResult("Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(gson.toJson(new ErrorResult("Error: unauthorized")));
        } catch (ForbiddenException e) {
            ctx.status(403);
            ctx.result(gson.toJson(new ErrorResult("Error: already taken")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + e.getMessage())));
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
