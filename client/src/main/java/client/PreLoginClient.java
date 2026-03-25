package client;

import java.util.Scanner;

public class PreLoginClient {

    private final ServerFacade facade;
    private final Scanner scanner;

    public PreLoginClient(int port) {
        this.facade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
    }

    public String eval(String input) {
        switch (input.toLowerCase()) {
            case "help" -> {
                return """
                        register - create an account
                        login - login to your account
                        quit - exit the program
                        help - show this menu""";
            }
            case "quit" -> {
                return "quit";
            }
            case "login" -> {
                return login();
            }
            case "register" -> {
                return register();
            }
            default -> {
                return "Unknown command. Type 'help' for options.";
            }
        }
    }

    private String login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            var authData = facade.login(username, password);
            new PostLoginClient(facade, authData.authToken()).run();
            return "Logged out successfully.";
        } catch (Exception e) {
            return "Login failed: " + e.getMessage();
        }
    }

    private String register() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        try {
            var authData = facade.register(username, password, email);
            new PostLoginClient(facade, authData.authToken()).run();
            return "Logged out successfully.";
        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }
}