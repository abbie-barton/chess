package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnCalculatorHelper {
    public static Collection<ChessMove> pawnCalculator(ChessPosition myPosition, ChessBoard board) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            moveList.addAll(whitePawnCalculatorHelper(myPosition, board, row, col));
        }

        else if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
            moveList.addAll(blackPawnCalculatorHelper(myPosition, board, row, col));
        }

        return moveList;
    }

    private static Collection<ChessMove> whitePawnCalculatorHelper(ChessPosition myPosition, ChessBoard board, int row, int col) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();
        // moving forward as WHITE
        if (row == 2) {
            // initial move
            if (board.getPiece(new ChessPosition(row + 2, col)) == null && board.getPiece(new ChessPosition(row + 1, col)) == null) {
                moveList.add(new ChessMove(myPosition, new ChessPosition(row + 2, col), null));
            }
        }
        if (row + 1 <= 8) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row + 1, col));
            if (newPiece == null) {
                // promotion
                if (row + 1 == 8) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), null));
                }
            }
        }
        // capturing as WHITE
        if (row + 1 <= 8 && col + 1 <= 8) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row + 1, col + 1));
            if (newPiece != null && newPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                // promotion
                if (row + 1 == 8) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col + 1), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col + 1), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col + 1), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col + 1), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col + 1), null));
                }
            }
        }
        if (row + 1 <= 8 && col - 1 >= 1) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row + 1, col - 1));
            if (newPiece != null && newPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                // promotion
                if (row + 1 == 8) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col - 1), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col - 1), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col - 1), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col - 1), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row + 1, col - 1), null));
                }
            }
        }
        return moveList;
    }

    private static Collection<ChessMove> blackPawnCalculatorHelper(ChessPosition myPosition, ChessBoard board, int row, int col) {
        Collection<ChessMove> moveList = new ArrayList<ChessMove>();
        // moving forward as BLACK
        if (row == 7) {
            // initial move
            if (board.getPiece(new ChessPosition(row - 2, col)) == null && board.getPiece(new ChessPosition(row - 1, col)) == null) {
                moveList.add(new ChessMove(myPosition, new ChessPosition(row - 2, col), null));
            }
        }
        if (row - 1 >= 1) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row - 1, col));
            if (newPiece == null) {
                // promotion
                if (row - 1 == 1) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), null));
                }
            }
        }
        // capturing as BLACK
        if (row - 1 >= 1 && col - 1 >= 1) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row - 1, col - 1));
            if (newPiece != null && newPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                // promotion
                if (row - 1 == 1) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col - 1), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col - 1), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col - 1), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col - 1), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col - 1), null));
                }
            }
        }
        if (row - 1 >= 1 && col + 1 <= 8) {
            ChessPiece newPiece = board.getPiece(new ChessPosition(row - 1, col + 1));
            if (newPiece != null && newPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                // promotion
                if (row - 1 == 1) {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), ChessPiece.PieceType.KNIGHT));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), ChessPiece.PieceType.ROOK));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), ChessPiece.PieceType.BISHOP));
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), ChessPiece.PieceType.QUEEN));
                } else {
                    moveList.add(new ChessMove(myPosition, new ChessPosition(row - 1, col + 1), null));
                }
            }
        }
        return moveList;
    }

}
