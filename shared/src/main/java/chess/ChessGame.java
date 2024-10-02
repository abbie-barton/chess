package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    public ChessBoard board;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) throws InvalidMoveException {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();
        ChessPiece currentPiece = board.getPiece(startPosition);

        moveList.addAll(currentPiece.pieceMoves(board, startPosition));

        for (ChessMove move : moveList) {
            this.makeMove(move);
        }

        return moveList;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currPiece = board.getPiece(move.getStartPosition());
        ChessPiece therePiece = null;

        if (currPiece.getTeamColor() != this.teamTurn) {
            // if the piece color is not this.teamTurn
            throw new InvalidMoveException();
        }
        if (board.getPiece(move.getEndPosition()) != null) {
            // if there is a piece at end position, set therePiece to that piece
            therePiece = board.getPiece(move.getEndPosition());
        }

        // add temp pieces to board
        if (move.getPromotionPiece() != null) {
            // if promotion piece, add that to the board
            board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
        }
        board.addPiece(move.getEndPosition(), currPiece);
        board.addPiece(move.getStartPosition(), null);

        // if invalid move, throw exception
        if (isInCheck(this.teamTurn) || isInCheckmate(this.teamTurn)) {
            // if in check or checkmate, undo those moves
            board.addPiece(move.getStartPosition(), currPiece);
            if (board.getPiece(move.getEndPosition()) != null) {
                board.addPiece(move.getEndPosition(), therePiece);
            } else {
                board.addPiece(move.getEndPosition(), null);
            }
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
