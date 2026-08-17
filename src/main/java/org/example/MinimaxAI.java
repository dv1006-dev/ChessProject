package org.example;

import piece.Piece;

import java.util.ArrayList;

public class MinimaxAI {
    private static final int INF = 1_000_000;
    private static final int CHECKMATE_SCORE = 100_000;

    private final GameLayout game;
    private final int aiColor;

    public MinimaxAI(GameLayout game, int aiColor) {
        this.game = game;
        this.aiColor = aiColor;
    }

    public void makeBestMove(int depth) {
        Move bestMove = findBestMove(depth);

        if (bestMove == null) {
            return;
        }

        makeMove(bestMove);
        syncRealPiecesWithSimPieces();
        game.currentColor = opposite(game.currentColor);
    }

    public Move findBestMove(int depth) {
        ArrayList<Move> moves = generateLegalMoves(aiColor);
        Move bestMove = null;
        int bestScore = -INF;

        for (Move move : moves) {
            makeMove(move);
            int score = -negamax(depth - 1, -INF, INF, opposite(aiColor));
            undoMove(move);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int negamax(int depth, int alpha, int beta, int colorToMove) {
        if (depth == 0) {
            return evaluateFor(colorToMove);
        }

        ArrayList<Move> moves = generateLegalMoves(colorToMove);

        if (moves.isEmpty()) {
            if (isKingInCheck(colorToMove)) {
                return -CHECKMATE_SCORE - depth;
            }
            return 0;
        }

        int bestScore = -INF;

        for (Move move : moves) {
            makeMove(move);
            int score = -negamax(depth - 1, -beta, -alpha, opposite(colorToMove));
            undoMove(move);

            bestScore = Math.max(bestScore, score);
            alpha = Math.max(alpha, score);

            if (alpha >= beta) {
                break;
            }
        }

        return bestScore;
    }

    private ArrayList<Move> generateLegalMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        ArrayList<Piece> positionPieces = new ArrayList<>(GameLayout.simPieces);

        for (Piece piece : positionPieces) {
            if (piece.color != color || !GameLayout.simPieces.contains(piece)) {
                continue;
            }

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    GameLayout.castlingP = null;
                    piece.hittingP = null;

                    if (!piece.canMove(col, row)) {
                        continue;
                    }

                    if (GameLayout.castlingP != null) {
                        GameLayout.castlingP = null;
                        continue;
                    }

                    Move move = new Move(piece, col, row, piece.hittingP);
                    makeMove(move);
                    boolean legal = !isKingInCheck(color);
                    undoMove(move);

                    if (legal) {
                        moves.add(move);
                    }
                }
            }
        }

        return moves;
    }

    private void makeMove(Move move) {
        move.oldCol = move.piece.col;
        move.oldRow = move.piece.row;
        move.oldPreCol = move.piece.preCol;
        move.oldPreRow = move.piece.preRow;
        move.oldX = move.piece.x;
        move.oldY = move.piece.y;
        move.oldMoved = move.piece.moved;
        move.oldTwoStepped = move.piece.twoStepped;
        move.oldHittingP = move.piece.hittingP;

        if (move.capturedPiece != null) {
            move.capturedIndex = GameLayout.simPieces.indexOf(move.capturedPiece);
            GameLayout.simPieces.remove(move.capturedPiece);
        }

        move.piece.col = move.toCol;
        move.piece.row = move.toRow;
        move.piece.x = move.piece.getX(move.toCol);
        move.piece.y = move.piece.getY(move.toRow);
        move.piece.preCol = move.toCol;
        move.piece.preRow = move.toRow;
        move.piece.moved = true;
        move.piece.hittingP = null;
    }

    private void undoMove(Move move) {
        move.piece.col = move.oldCol;
        move.piece.row = move.oldRow;
        move.piece.preCol = move.oldPreCol;
        move.piece.preRow = move.oldPreRow;
        move.piece.x = move.oldX;
        move.piece.y = move.oldY;
        move.piece.moved = move.oldMoved;
        move.piece.twoStepped = move.oldTwoStepped;
        move.piece.hittingP = move.oldHittingP;

        if (move.capturedPiece != null && move.capturedIndex >= 0) {
            GameLayout.simPieces.add(move.capturedIndex, move.capturedPiece);
        }
    }

    private boolean isKingInCheck(int kingColor) {
        Piece king = null;

        for (Piece piece : GameLayout.simPieces) {
            if (piece.type == Type.KING && piece.color == kingColor) {
                king = piece;
                break;
            }
        }

        if (king == null) {
            return true;
        }

        for (Piece piece : GameLayout.simPieces) {
            if (piece.color == kingColor) {
                continue;
            }

            GameLayout.castlingP = null;
            piece.hittingP = null;

            if (piece.canMove(king.col, king.row)) {
                return true;
            }
        }

        return false;
    }

    private int evaluateFor(int color) {
        int score = 0;

        for (Piece piece : GameLayout.simPieces) {
            int value = getPieceValue(piece);

            if (piece.color == color) {
                score += value;
            }
            else {
                score -= value;
            }
        }

        return score;
    }

    private int getPieceValue(Piece piece) {
        return switch (piece.type) {
            case PAWN -> 100;
            case KNIGHT -> 320;
            case BISHOP -> 330;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 20_000;
        };
    }

    private int opposite(int color) {
        return color == GameLayout.WHITE ? GameLayout.BLACK : GameLayout.WHITE;
    }

    private void syncRealPiecesWithSimPieces() {
        GameLayout.pieces.clear();
        GameLayout.pieces.addAll(GameLayout.simPieces);
    }

    public static class Move {
        public final Piece piece;
        public final int toCol;
        public final int toRow;
        public final Piece capturedPiece;

        private int oldCol;
        private int oldRow;
        private int oldPreCol;
        private int oldPreRow;
        private int oldX;
        private int oldY;
        private boolean oldMoved;
        private boolean oldTwoStepped;
        private Piece oldHittingP;
        private int capturedIndex = -1;

        private Move(Piece piece, int toCol, int toRow, Piece capturedPiece) {
            this.piece = piece;
            this.toCol = toCol;
            this.toRow = toRow;
            this.capturedPiece = capturedPiece;
        }
    }
}
