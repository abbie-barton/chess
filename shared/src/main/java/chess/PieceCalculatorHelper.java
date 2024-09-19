package chess;

import java.util.Collection;
import java.util.ArrayList;

import static chess.PawnCalculatorHelper.pawnCalculator;

public class PieceCalculatorHelper {

    public static Collection<ChessMove> calculatePieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        ChessPiece.PieceType currentPieceType = currentPiece.getPieceType();
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        if (currentPieceType == ChessPiece.PieceType.BISHOP) {
            moveList = bishopCalculator(myPosition, board);
        }
        else if (currentPieceType == ChessPiece.PieceType.ROOK) {
            moveList = rookCalculator(myPosition, board);
        }
        else if (currentPieceType == ChessPiece.PieceType.QUEEN) {
            moveList = queenCalculator(myPosition, board);
        }
        else if (currentPieceType == ChessPiece.PieceType.KNIGHT) {
            moveList = knightCalculator(myPosition, board);
        }
        else if (currentPieceType == ChessPiece.PieceType.KING) {
            moveList = kingCalculator(myPosition, board);
        }
        else if (currentPieceType == ChessPiece.PieceType.PAWN) {
            moveList = pawnCalculator(myPosition, board);
        }

        return moveList;
    }

    public static Collection<ChessMove> bishopCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        // Add moves for each direction for the bishop
        addMovesInDirection(moveList, myPosition, board, 1, 1);   // Diagonal up-right
        addMovesInDirection(moveList, myPosition, board, 1, -1);  // Diagonal up-left
        addMovesInDirection(moveList, myPosition, board, -1, -1); // Diagonal down-left
        addMovesInDirection(moveList, myPosition, board, -1, 1);  // Diagonal down-right

        return moveList;
    }

    public static Collection<ChessMove> rookCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        addMovesInDirection(moveList, myPosition, board, 1, 0);   // up
        addMovesInDirection(moveList, myPosition, board, -1, 0);  // down
        addMovesInDirection(moveList, myPosition, board, 0, -1); // left
        addMovesInDirection(moveList, myPosition, board, 0, 1);  // right

        return moveList;
    }

    public static Collection<ChessMove> queenCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        moveList.addAll(rookCalculator(myPosition, board));
        moveList.addAll(bishopCalculator(myPosition, board));

        return moveList;
    }

    public static Collection<ChessMove> knightCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        addLimitedMovesInDirection(moveList, myPosition, board, 2, 1);
        addLimitedMovesInDirection(moveList, myPosition, board, 2, -1);
        addLimitedMovesInDirection(moveList, myPosition, board, -2, 1);
        addLimitedMovesInDirection(moveList, myPosition, board, -2, -1);
        addLimitedMovesInDirection(moveList, myPosition, board, 1, 2);
        addLimitedMovesInDirection(moveList, myPosition, board, 1, -2);
        addLimitedMovesInDirection(moveList, myPosition, board, -1, 2);
        addLimitedMovesInDirection(moveList, myPosition, board, -1, -2);

        return moveList;
    }

    public static Collection<ChessMove> kingCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        addLimitedMovesInDirection(moveList, myPosition, board, 1, 1);
        addLimitedMovesInDirection(moveList, myPosition, board, 1, -1);
        addLimitedMovesInDirection(moveList, myPosition, board, 1, 0);
        addLimitedMovesInDirection(moveList, myPosition, board, -1, 1);
        addLimitedMovesInDirection(moveList, myPosition, board, -1, -1);
        addLimitedMovesInDirection(moveList, myPosition, board, -1, 0);
        addLimitedMovesInDirection(moveList, myPosition, board, 0, 1);
        addLimitedMovesInDirection(moveList, myPosition, board, 0, -1);

        return moveList;
    }

    private static void addMovesInDirection(Collection<ChessMove> moveList, ChessPosition myPosition, ChessBoard board, int rowIncrement, int colIncrement) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int counter = 1;
        int isValid = 1;

        while (isValid != 0) {
            int newRow = row + counter * rowIncrement;
            int newCol = col + counter * colIncrement;
            // Check if the new position is within bounds and if a piece is not blocking the way
            if ((newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8)) {
                ChessPiece newPiece = board.getPiece(new ChessPosition(newRow, newCol));
                // logic to add a possible move to capture a piece of the opposing color, but go no farther
                if (newPiece != null && board.getPiece(myPosition).getTeamColor() != newPiece.getTeamColor()) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
                    isValid = 0;
                // if there is no piece at that position, add a move
                } else if (newPiece == null) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
                } else {
                    isValid = 0;
                }
            } else {
                isValid = 0;
            }
            counter += 1;
        }
    }

    private static void addLimitedMovesInDirection(Collection<ChessMove> moveList, ChessPosition myPosition, ChessBoard board, int rowIncrement, int colIncrement) {
        // go forward only one time (ex for knight and king)
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int newRow = row + rowIncrement;
        int newCol = col + colIncrement;

        // Check if the new position is within bounds and if a piece is not blocking the way
        if ((newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8)) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(newRow, newCol));
            // logic to add a possible move to capture a piece of the opposing color, but go no farther
            if (newPiece != null && board.getPiece(myPosition).getTeamColor() != newPiece.getTeamColor()) {
                moveList.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
                // if there is no piece at that position, add a move
            } else if (newPiece == null) {
                moveList.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
            }
        }
    }
}
