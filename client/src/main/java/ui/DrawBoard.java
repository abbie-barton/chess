package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class DrawBoard {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;
    private static final int LINE_WIDTH_IN_PADDED_CHARS = 1;

    // Padded characters.
    private static final String EMPTY = "   ";

    public static void main(GameData game, boolean whiteAtBottom) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        drawHeaders(out, whiteAtBottom);

        drawChessBoard(out, game.game(), whiteAtBottom);

        drawHeaders(out, whiteAtBottom);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawHeaders(PrintStream out, boolean whiteAtBottom) {
        setBlack(out);

        String[] headers;
        if (whiteAtBottom) {
            headers = new String[]{ " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h " };
        } else {
            headers = new String[]{ " h ", " g ", " f ", " e ", " d ", " c ", " b ", " a "};
        }
        out.print(EMPTY.repeat(2));
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            drawHeader(out, headers[boardCol]);

            if (boardCol < BOARD_SIZE_IN_SQUARES - 1) {
                out.print(EMPTY.repeat(LINE_WIDTH_IN_PADDED_CHARS));
            }
        }

        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
        int suffixLength = 0;

        out.print(EMPTY.repeat(prefixLength));
        printHeaderText(out, headerText);
        out.print(EMPTY.repeat(suffixLength));
    }

    private static void printHeaderText(PrintStream out, String player) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);

        out.print(player);

        setBlack(out);
    }

    private static void drawChessBoard(PrintStream out, ChessGame game, boolean whiteAtBottom) {
        String[] rowLabels;
        if (whiteAtBottom) {
            rowLabels = new String[]{"8", "7", "6", "5", "4", "3", "2", "1"};
        } else {
            rowLabels = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
        }

        for (int boardRow = 0; boardRow < BOARD_SIZE_IN_SQUARES; ++boardRow) {
            drawRowOfSquares(out, rowLabels[boardRow], game.getBoard(), whiteAtBottom);
        }
    }

    private static void drawRowOfSquares(PrintStream out, String label, ChessBoard board, boolean whiteAtBottom) {
        boolean setInitialDark;
        if (whiteAtBottom) {
            setInitialDark = Integer.parseInt(label) % 2 == 0;
        } else {
            setInitialDark = Integer.parseInt(label) % 2 == 1;
        }

        int oppositeCounter = 8;
        for (int squareRow = 0; squareRow < SQUARE_SIZE_IN_PADDED_CHARS; ++squareRow) {
            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                printPlayer(out, "  " + label + "   ");
            } else {
                out.print(EMPTY.repeat(2));
            }
            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
                if ((setInitialDark && boardCol % 2 == 0) || (!setInitialDark && boardCol % 2 == 1)) {
                    setBrown(out);
                } else {
                    setDarkBrown(out);
                }

                if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                    int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
                    int suffixLength = SQUARE_SIZE_IN_PADDED_CHARS - prefixLength - 1;

                    out.print(EMPTY.repeat(prefixLength));
                    int useCol;
                    if (whiteAtBottom) {
                        useCol = boardCol+1;
                    } else {
                        useCol = oppositeCounter;
                    }
                    ChessPiece maybePiece = board.getPiece(new ChessPosition(Integer.parseInt(label), useCol));
                    oppositeCounter--;
                    boolean blackTeam = maybePiece.getTeamColor() == ChessGame.TeamColor.BLACK;
                    String gamePiece = switch (maybePiece.getPieceType()) {
                        case ChessPiece.PieceType.KING -> blackTeam ? BLACK_KING : WHITE_KING;
                        case ChessPiece.PieceType.QUEEN -> blackTeam ? BLACK_QUEEN : WHITE_QUEEN;
                        case ChessPiece.PieceType.BISHOP -> blackTeam ? BLACK_BISHOP : WHITE_BISHOP;
                        case ChessPiece.PieceType.ROOK -> blackTeam ? BLACK_ROOK : WHITE_ROOK;
                        case ChessPiece.PieceType.KNIGHT -> blackTeam ? BLACK_KNIGHT : WHITE_KNIGHT;
                        case ChessPiece.PieceType.PAWN -> blackTeam ? BLACK_PAWN : WHITE_PAWN;
                        case null -> "   ";
                    };
                    printPlayer(out, gamePiece);
                    out.print(EMPTY.repeat(suffixLength));
                }
                else {
                    out.print(EMPTY.repeat(SQUARE_SIZE_IN_PADDED_CHARS));
                }

                setBlack(out);
            }

            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                printPlayer(out, "  " + label + "   ");
            } else {
                out.print(EMPTY.repeat(2));
            }

            out.println();
        }
    }

    private static void setDarkBrown(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_BROWN);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBrown(PrintStream out) {
        out.print(SET_BG_COLOR_BROWN);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void printPlayer(PrintStream out, String player) {
        out.print(SET_TEXT_COLOR_WHITE);

        out.print(player);

    }
}
