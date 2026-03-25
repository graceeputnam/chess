package client;

import java.util.Scanner;

public class ChessClient {

    private final PreLoginClient preLoginClient;

    public ChessClient(int port) {
        this.preLoginClient = new PreLoginClient(port);
    }

    public void run() {
        System.out.println("Welcome to Chess! Type 'help' to get started.");
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            String result = preLoginClient.eval(input);
            System.out.println(result);
            if (result.equals("quit")) {
                break;
            }
        }
    }
}