package client;

import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostLoginClient {

    private final ServerFacade facade;
    private final String authToken;
    private final String username;
    private final Scanner scanner;
    private List<GameData> lastGameList = new ArrayList<>();

    public PostLoginClient(ServerFacade facade, String authToken, String username) {
        this.facade = facade;
        this.authToken = authToken;
        this.username = username;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Logged in as " + username + "! Type 'help' to see commands.");
        while (true) {
            System.out.print("[" + username + "] >>> ");
            String result = eval(scanner.nextLine().trim());
            System.out.println(result);
            if (result.equals("logged out")) {
                break;
            }
        }
    }

    private String eval(String input) {
        return switch (input.toLowerCase()) {
            case "help" -> """
                    list - list all games
                    create - create a new game
                    play - join a game
                    observe - observe a game
                    logout - logout
                    help - show this menu""";
            case "logout" -> logout();
            case "create" -> createGame();
            case "list" -> listGames();
            case "play" -> playGame();
            case "observe" -> observeGame();
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String logout() {
        try {
            facade.logout(authToken);
            return "logged out";
        } catch (Exception e) {
            return "Logout failed: " + e.getMessage();
        }
    }

    private String createGame() {
        System.out.print("Game name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            return "Game name cannot be empty.";
        }
        try {
            facade.createGame(authToken, name);
            return "Game created!";
        } catch (Exception e) {
            return "Failed to create game: " + e.getMessage();
        }
    }

    private String listGames() {
        try {
            lastGameList = facade.listGames(authToken);
            if (lastGameList.isEmpty()) {
                return "No games available.";
            }
            var sb = new StringBuilder();
            for (int i = 0; i < lastGameList.size(); i++) {
                var game = lastGameList.get(i);
                sb.append(i + 1).append(". ").append(game.gameName())
                        .append(" | White: ").append(game.whiteUsername() != null ? game.whiteUsername() : "open")
                        .append(" | Black: ").append(game.blackUsername() != null ? game.blackUsername() : "open")
                        .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "Failed to list games: " + e.getMessage();
        }
    }

    private String playGame() {
        if (lastGameList.isEmpty()) {
            return "No games to join. Please list games first.";
        }
        System.out.print("Game number: ");
        String number = scanner.nextLine().trim();
        System.out.print("Color (WHITE/BLACK): ");
        String color = scanner.nextLine().trim().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Invalid color. Please enter WHITE or BLACK.";
        }
        try {
            int index = Integer.parseInt(number) - 1;
            if (index < 0 || index >= lastGameList.size()) {
                return "Invalid game number.";
            }
            facade.joinGame(authToken, lastGameList.get(index).gameID(), color);
            BoardDrawer.draw(color.equals("BLACK"));
            return "Joined game!";
        } catch (NumberFormatException e) {
            return "Please enter a valid number.";
        } catch (Exception e) {
            return "Failed to join game: " + e.getMessage();
        }
    }

    private String observeGame() {
        if (lastGameList.isEmpty()) {
            return "No games to observe. Please list games first.";
        }
        System.out.print("Game number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index < 0 || index >= lastGameList.size()) {
                return "Invalid game number.";
            }
            BoardDrawer.draw(false);
            return "Observing game!";
        } catch (NumberFormatException e) {
            return "Please enter a valid number.";
        } catch (Exception e) {
            return "Failed to observe game: " + e.getMessage();
        }
    }
}