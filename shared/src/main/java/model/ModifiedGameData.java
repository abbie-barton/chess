package model;

import chess.ChessGame;

public record ModifiedGameData(int gameID, String whiteUsername, String blackUsername,
                               String gameName, ChessGame game, int is_over) {
}
