package org.example;

import piece.*;

import java.util.ArrayList;

public class FENLoader {

    public static void loadFEN(GameLayout game, String fen) {

        GameLayout.pieces.clear();
        GameLayout.simPieces.clear();

        String[] parts = fen.split(" ");

        String board = parts[0];
        String turn = parts[1];

        String[] rows = board.split("/");

        for (int row = 0; row < 8; row++) {

            int col = 0;

            for (int i = 0; i < rows[row].length(); i++) {

                char symbol = rows[row].charAt(i);

                if (Character.isDigit(symbol)) {

                    col += Character.getNumericValue(symbol);

                } else {

                    int color = Character.isUpperCase(symbol)
                            ? GameLayout.WHITE
                            : GameLayout.BLACK;

                    char pieceType =
                            Character.toLowerCase(symbol);

                    Piece piece = createPiece(
                            pieceType,
                            color,
                            col,
                            row
                    );

                    if (piece != null) {
                        GameLayout.pieces.add(piece);
                        col++;
                    }
                }
            }
        }

        GameLayout.simPieces.addAll(GameLayout.pieces);

        GameLayout.castlingP = null;

        game.currentColor = turn.equals("w")
                        ? GameLayout.WHITE
                        : GameLayout.BLACK;
    }

    private static Piece createPiece(
            char type,
            int color,
            int col,
            int row) {

        return switch (type) {

            case 'p' -> new Pawn(color, col, row);

            case 'n' -> new Knight(color, col, row);

            case 'b' -> new Bishop(color, col, row);

            case 'r' -> new Rook(color, col, row);

            case 'q' -> new Queen(color, col, row);

            case 'k' -> new King(color, col, row);

            default -> null;
        };
    }

}
