package model;

import chess.ChessGame;

public record UpdateGameRequest(int gameID, ChessGame game) {
}
