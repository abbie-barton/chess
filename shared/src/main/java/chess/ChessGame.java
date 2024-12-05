package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = board.getPiece(startPosition);

        Collection<ChessMove> moveList = new ArrayList<ChessMove>(currentPiece.pieceMoves(board, startPosition));
        Collection<ChessMove> validMoves = new ArrayList<ChessMove>();

        for (ChessMove move : moveList) {
            // make move checks if each move is valid or not - if it is invalid, then remove it from moveList
            try {
                this.checkMove(move);
                validMoves.add(move);
            } catch (Exception InvalidMoveException) {
                // move is invalid
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currPiece = board.getPiece(move.getStartPosition());

        if (currPiece == null) {
            // no starting piece
            throw new InvalidMoveException("Error: No starting piece provided. Expected: make move <START_POSITION> <END_POSITION>");
        }
        if (currPiece.getTeamColor() != this.teamTurn) {
            // if the piece color is not this.teamTurn
            throw new InvalidMoveException("Error: It isn't your turn!");
        }

        try {
            checkMove(move);
            board.addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null) {
                board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
            } else {
                board.addPiece(move.getEndPosition(), currPiece);
            }
            // set team turn color
            if (this.teamTurn == TeamColor.WHITE) {
                this.teamTurn = TeamColor.BLACK;
            } else {
                this.teamTurn = TeamColor.WHITE;
            }
        } catch (Exception InvalidMoveException) {
            throw new InvalidMoveException("Error: Invalid move. Try <highlight> to see valid moves");
        }
    }

    public void checkMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currPiece = board.getPiece(move.getStartPosition());
        ChessPiece therePiece = null;

        if (move.getEndPosition().getColumn() > 8 || move.getEndPosition().getColumn() < 1 ||
            move.getEndPosition().getRow() > 8 || move.getEndPosition().getRow() < 1) {
            // end position is out of bounds
            throw new InvalidMoveException("Error: End position is out of bounds.");
        }

        // get possible moves & check if endPosition matches possible moves
        Collection<ChessMove> possibleMoves = board.getPiece(move.getStartPosition()).pieceMoves(board, move.getStartPosition());
        boolean valid = false;
        for (ChessMove m : possibleMoves) {
            if (move.getEndPosition().getRow() == m.getEndPosition().getRow() &&
                move.getEndPosition().getColumn() == m.getEndPosition().getColumn()) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new InvalidMoveException("Error: Invalid move. Try <highlight> to see valid moves");
        }

        if (board.getPiece(move.getEndPosition()) != null) {
            // if there is a piece at end position, set therePiece to that piece
            therePiece = board.getPiece(move.getEndPosition());
        }

        // add temp pieces to board
        board.addPiece(move.getEndPosition(), currPiece);
        board.addPiece(move.getStartPosition(), null);

        // if invalid move, throw exception
        if (isInCheck(currPiece.getTeamColor()) || isInCheckmate(currPiece.getTeamColor())) {
            // if in check or checkmate, undo those moves
            board.addPiece(move.getStartPosition(), currPiece);
            if (board.getPiece(move.getEndPosition()) != null) {
                board.addPiece(move.getEndPosition(), therePiece);
            } else {
                board.addPiece(move.getEndPosition(), null);
            }
            throw new InvalidMoveException("Error: no more moves can be made");
        }

        // undo - the move is valid
        board.addPiece(move.getStartPosition(), currPiece);
        if (board.getPiece(move.getEndPosition()) != null) {
            board.addPiece(move.getEndPosition(), therePiece);
        } else {
            board.addPiece(move.getEndPosition(), null);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // find the position of the king
        ChessPosition kingPosition = findKingPosition(teamColor);

        if (kingPosition == null) {
            return false;
        }

        // find all the pieces on the opposite team
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece tempPiece = board.getPiece(new ChessPosition(i, j));
                if (tempPiece != null && tempPiece.getTeamColor() != teamColor) {
                    // call pieceMoves on each and see if the king is at the endPosition of the move
                    Collection<ChessMove> moves = tempPiece.pieceMoves(board, new ChessPosition(i, j));
                    if (kingAtEndPosition(moves, kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean kingAtEndPosition (Collection<ChessMove> moves, ChessPosition kingPosition) {
        // call pieceMoves on each and see if the king is at the endPosition of the move
        for (ChessMove move : moves) {
            // if king is at endPosition of the move, king is in check
            if (move.getEndPosition().getRow() == kingPosition.getRow() &&
                    move.getEndPosition().getColumn() == kingPosition.getColumn()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);

        if (kingPosition == null || !isInCheck(teamColor)) {
            // no king on the board or king is not in check
            return false;
        }

        Collection<ChessMove> validMoves = new ArrayList<>(this.findValidMovesForTeam(teamColor));

        // if no valid moves - king is in checkmate
        return validMoves.isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // no valid moves but your king is not currently threatened
        ChessPosition kingPosition = findKingPosition(teamColor);

        if (kingPosition == null || isInCheck(teamColor)) {
            // no king on the board or king is in check
            return false;
        }

        Collection<ChessMove> validMoves = new ArrayList<>(this.findValidMovesForTeam(teamColor));

        // if no valid moves - king is in checkmate
        return validMoves.isEmpty();
    }

    public Collection<ChessMove> findValidMovesForTeam(TeamColor teamColor) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece tempPiece = board.getPiece(new ChessPosition(i, j));
                if (tempPiece != null && tempPiece.getTeamColor() == teamColor) {
                    // call validMoves on each piece on current team
                    validMoves.addAll(this.validMoves(new ChessPosition(i, j)));
                }
            }
        }

        return validMoves;
    }

    public ChessPosition findKingPosition(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // find the position of the king
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece tempPiece = board.getPiece(new ChessPosition(i, j));
                if (tempPiece != null && tempPiece.getPieceType() == ChessPiece.PieceType.KING &&
                    tempPiece.getTeamColor() == teamColor) {
                    kingPosition = new ChessPosition(i, j);
                    break;
                }
            }
        }

        return kingPosition;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
