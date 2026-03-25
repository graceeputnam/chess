package service;
import model.GameData;

public record ListGamesResult(GameData[] games, String message) {
}
