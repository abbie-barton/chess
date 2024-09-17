package chess;

import java.util.Collection;
import java.util.ArrayList;

public class PieceCalculatorHelper {

    static public void test() {
        System.out.println("pieceCalculatorHelper test");
    }

    public static Collection<ChessMove> calculatePieceMoves(ChessBoard board, ChessPosition myPosition) {
        System.out.println("pieceCalculatorHelper calculatePieceMoves");

        ChessPiece currentPiece = board.getPiece(myPosition);
        System.out.println("piece: " + currentPiece);
        ChessPiece.PieceType currentPieceType = currentPiece.getPieceType();
        ChessGame.TeamColor currentPieceColor = currentPiece.getTeamColor();
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();

        if (currentPieceType == ChessPiece.PieceType.BISHOP) {
            moveList = bishopCalculator(currentPiece, currentPieceColor, myPosition, board);
        }
        return moveList;
    }

    public static Collection<ChessMove> bishopCalculator(ChessPiece currentPiece, ChessGame.TeamColor currentPieceColor, ChessPosition myPosition, ChessBoard board) {
        System.out.println("pieceCalculatorHelper bishopCalculator");

        Collection<ChessMove> moveList = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        // Add moves for each direction for the bishop
        addMovesInDirection(moveList, myPosition, board, row, col, 1, 1);   // Diagonal up-right
        addMovesInDirection(moveList, myPosition, board, row, col, 1, -1);  // Diagonal up-left
        addMovesInDirection(moveList, myPosition, board, row, col, -1, -1); // Diagonal down-left
        addMovesInDirection(moveList, myPosition, board, row, col, -1, 1);  // Diagonal down-right

        return moveList;
    }

    private static void addMovesInDirection(Collection<ChessMove> moveList, ChessPosition myPosition, ChessBoard board, int row, int col, int rowIncrement, int colIncrement) {
        int counter = 1;
        int isValid = 1;

        while (isValid != 0) {
            int newRow = row + counter * rowIncrement;
            int newCol = col + counter * colIncrement;
            // Check if the new position is within bounds and if a piece is not blocking the way
            if ((newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) && board.getPiece(new ChessPosition(newRow, newCol)) == null) {
                moveList.add(new ChessMove(myPosition, new ChessPosition(newRow, newCol), null));
            } else {
                isValid = 0;
            }
            counter += 1;
        }
    }
}
