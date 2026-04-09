package client;

import chess.*;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayClient {

    private final WebSocketFacade ws;
    private final String authToken;
    private final int gameID;
    private final String username;
    private final boolean isBlack;
    private final boolean isObserver;
    private ChessGame currentGame;
    private final Scanner scanner = new Scanner(System.in);

    public GameplayClient(int port, String authToken, int gameID, String username, boolean isBlack, boolean isObserver) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.username = username;
        this.isBlack = isBlack;
        this.isObserver = isObserver;
        this.ws = new WebSocketFacade(port, this::handleMessage);
        ws.sendConnect(authToken, gameID);
    }

    public void run() {
        System.out.println("Joined game! Type 'help' to see commands.");
        while (true) {
            System.out.print("[IN-GAME] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "help" -> printHelp();
                case "redraw" -> redrawBoard();
                case "leave" -> {
                    leave();
                    return;
                }
                case "move" -> makeMove();
                case "resign" -> resign();
                case "highlight" -> highlight();
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                help - show this menu
                redraw - redraw the chess board
                move - make a move
                highlight - highlight legal moves for a piece
                resign - forfeit the game
                leave - leave the game""");
    }

    private void redrawBoard() {
        if (currentGame != null) {
            BoardDrawer.draw(currentGame, isBlack, null);
        }
    }

    private void leave() {
        try {
            ws.sendLeave(authToken, gameID);
            ws.close();
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void makeMove() {
        if (isObserver) {
            System.out.println("Observers cannot make moves.");
            return;
        }
        System.out.print("Start position (e.g. e2): ");
        String start = scanner.nextLine().trim().toLowerCase();
        System.out.print("End position (e.g. e4): ");
        String end = scanner.nextLine().trim().toLowerCase();

        ChessPosition startPos = parsePosition(start);
        ChessPosition endPos = parsePosition(end);

        if (startPos == null || endPos == null) {
            System.out.println("Invalid position format. Use format like 'e2'.");
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (isPromotionMove(startPos, endPos)) {
            System.out.print("Promotion piece (QUEEN/ROOK/BISHOP/KNIGHT): ");
            String promo = scanner.nextLine().trim().toUpperCase();
            promotion = switch (promo) {
                case "QUEEN" -> ChessPiece.PieceType.QUEEN;
                case "ROOK" -> ChessPiece.PieceType.ROOK;
                case "BISHOP" -> ChessPiece.PieceType.BISHOP;
                case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
                default -> null;
            };
        }

        try {
            ws.sendMakeMove(authToken, gameID, new ChessMove(startPos, endPos, promotion));
        } catch (Exception e) {
            System.out.println("Error making move: " + e.getMessage());
        }
    }

    private void resign() {
        if (isObserver) {
            System.out.println("Observers cannot resign.");
            return;
        }
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) {
            try {
                ws.sendResign(authToken, gameID);
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        }
    }

    private void highlight() {
        if (currentGame == null) return;
        System.out.print("Piece position (e.g. e2): ");
        String input = scanner.nextLine().trim().toLowerCase();
        ChessPosition pos = parsePosition(input);
        if (pos == null) {
            System.out.println("Invalid position format.");
            return;
        }
        Collection<ChessMove> moves = currentGame.validMoves(pos);
        if (moves == null || moves.isEmpty()) {
            System.out.println("No legal moves for that piece.");
            return;
        }
        Collection<ChessPosition> highlights = moves.stream()
                .map(ChessMove::getEndPosition)
                .toList();
        BoardDrawer.draw(currentGame, isBlack, highlights);
    }

    private void handleMessage(ServerMessage message) {
        switch (message.serverMessageType) {
            case LOAD_GAME -> {
                currentGame = message.game;
                BoardDrawer.draw(currentGame, isBlack, null);
                System.out.print("[IN-GAME] >>> ");
            }
            case NOTIFICATION -> {
                System.out.println("\n*** " + message.message + " ***");
                System.out.print("[IN-GAME] >>> ");
            }
            case ERROR -> {
                System.out.println("\n[ERROR] " + message.errorMessage);
                System.out.print("[IN-GAME] >>> ");
            }
        }
    }

    private ChessPosition parsePosition(String input) {
        if (input == null || input.length() != 2) return null;
        int col = input.charAt(0) - 'a' + 1;
        int row = input.charAt(1) - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) return null;
        return new ChessPosition(row, col);
    }

    private boolean isPromotionMove(ChessPosition start, ChessPosition end) {
        if (currentGame == null) return false;
        ChessPiece piece = currentGame.getBoard().getPiece(start);
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) return false;
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1);
    }
}