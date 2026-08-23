package org.example;

import piece.Piece;
import piece.Queen;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MinimaxAI {
    private static final int INF = 1_000_000;
    private static final int CHECKMATE_SCORE = 100_000;

    public enum SearchMode {
        PLAIN_NEGAMAX,
        ALPHA_BETA,
        ALPHA_BETA_ORDERED
    }

    private final GameLayout game;
    private final int aiColor;
    private final SearchMode searchMode;
    private long nodesSearched;
    private long betaCutoffs;
    private long searchStartTime;
    private long lastSearchNanos;
    private int lastBestScore;
    private int rootMoves;


    public MinimaxAI(GameLayout game, int aiColor) {
        this(game, aiColor, SearchMode.ALPHA_BETA_ORDERED);
    }

    public MinimaxAI(GameLayout game, int aiColor, SearchMode searchMode) {
        this.game = game;
        this.aiColor = aiColor;
        this.searchMode = searchMode;
    }

    public Move makeBestMove(int depth) {
        Move bestMove = findBestMove(depth);

        if (bestMove == null) {
            return null;
        }

        makeMove(bestMove);
        syncRealPiecesWithSimPieces();
        //game.currentColor = opposite(game.currentColor);
        updateCheckingPiece();
        return bestMove;
    }

    public Move findBestMove(int depth) {
        nodesSearched = 0;
        betaCutoffs = 0;
        lastSearchNanos = 0;
        searchStartTime = System.nanoTime();

        ArrayList<Move> moves = generateLegalMoves(aiColor, usesMoveOrdering());
        rootMoves = moves.size();
        Move bestMove = null;
        int bestScore = -INF;

        for (Move move : moves) {
            makeMove(move);
            int score;

            if (searchMode == SearchMode.PLAIN_NEGAMAX) {
                score = -plainNegamax(depth - 1, opposite(aiColor));
            }
            else {
                score = -alphaBetaNegamax(depth - 1, -INF, INF, opposite(aiColor));
            }

            undoMove(move);

            if (bestMove == null ||
                    score > bestScore ||
                    (usesMoveOrdering() && score == bestScore &&
                            getMoveOrderScore(move) > getMoveOrderScore(bestMove))) {
                bestScore = score;
                bestMove = move;
            }
        }

        lastBestScore = bestScore;
        lastSearchNanos = System.nanoTime() - searchStartTime;

        return bestMove;
    }
    public static void saveBenchmarkResults(
            ArrayList<BenchmarkResult> results) {

        String fileName = "benchmark_results.csv";

        try {
            boolean fileExists = new java.io.File(fileName).exists();

            PrintWriter writer = new PrintWriter(
                    new FileWriter(fileName, true)
            );

            if (!fileExists) {
                writer.println(
                        "Position,Algorithm,Depth,Score,RootMoves,Nodes,BetaCutoffs,TimeMillis,NodesPerSecond"
                );
            }

            for (BenchmarkResult result : results) {

                writer.println(
                        result.positionNumber + "," +
                                result.mode + "," +
                                result.depth + "," +
                                result.score + "," +
                                result.rootMoves + "," +
                                result.nodes + "," +
                                result.betaCutoffs + "," +
                                result.timeMillis + "," +
                                result.nodesPerSecond
                );
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("Error saving benchmark results:");
            e.printStackTrace();
        }
    }
    public static class BenchmarkResult {
        public SearchMode mode;
        public int depth;
        public int score;
        public int rootMoves;
        public long nodes;
        public long betaCutoffs;
        public double timeMillis;
        public long nodesPerSecond;
        public int positionNumber;
        public BenchmarkResult(
                int positionNumber,
                SearchMode mode,
                int depth,
                int score,
                int rootMoves,
                long nodes,
                long betaCutoffs,
                double timeMillis,
                long nodesPerSecond) {

            this.positionNumber = positionNumber;
            this.mode = mode;
            this.depth = depth;
            this.score = score;
            this.rootMoves = rootMoves;
            this.nodes = nodes;
            this.betaCutoffs = betaCutoffs;
            this.timeMillis = timeMillis;
            this.nodesPerSecond = nodesPerSecond;
        }
    }
    public static void benchmarkPositionDatabase(
            GameLayout game,
            String fileName,
            int depth) {

        try {

            java.io.BufferedReader reader =
                    new java.io.BufferedReader(
                            new java.io.FileReader(fileName)
                    );

            String fen;
            int positionNumber = 1;

            while ((fen = reader.readLine()) != null) {

                // Skip empty lines
                if (fen.trim().isEmpty()) {
                    continue;
                }

                System.out.println(
                        "Running position " + positionNumber
                );

                // Load this position
                FENLoader.loadFEN(game, fen);

                // Run all 3 AI versions
                ArrayList<BenchmarkResult> results = runBenchmark(
                        game,
                        game.currentColor,
                        depth,
                        positionNumber
                );
                saveBenchmarkResults(results);

                positionNumber++;
            }

            reader.close();

            System.out.println("Done!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<BenchmarkResult> runBenchmark(GameLayout game, int aiColor, int depth, int positionNumber) {
        PositionSnapshot originalPosition = new PositionSnapshot();

        ArrayList<BenchmarkResult> results = new ArrayList<>();


        for (SearchMode mode : SearchMode.values()) {
            originalPosition.restore();

            MinimaxAI benchmarkAi = new MinimaxAI(game, aiColor, mode);
            Move bestMove = benchmarkAi.findBestMove(depth);

            results.add(new BenchmarkResult(positionNumber, mode, depth,
                    benchmarkAi.getLastBestScore(),
                    benchmarkAi.getRootMoves(),
                    benchmarkAi.getNodesSearched(),
                    benchmarkAi.getBetaCutoffs(),
                    benchmarkAi.getLastSearchMillis(),
                    benchmarkAi.getNodesPerSecond()
            ));
        }

        originalPosition.restore();
        return results;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public long getNodesSearched() {
        return nodesSearched;
    }

    public long getBetaCutoffs() {
        return betaCutoffs;
    }

    public int getLastBestScore() {
        return lastBestScore;
    }

    public int getRootMoves() {
        return rootMoves;
    }

    public double getLastSearchMillis() {
        return lastSearchNanos / 1_000_000.0;
    }

    public long getNodesPerSecond() {
        double elapsedSeconds = lastSearchNanos / 1_000_000_000.0;
        return elapsedSeconds > 0
                ? (long) (nodesSearched / elapsedSeconds)
                : 0;
    }

    private int plainNegamax(int depth, int colorToMove) {
        nodesSearched++;

        if (depth == 0) {
            return evaluateFor(colorToMove);
        }

        ArrayList<Move> moves = generateLegalMoves(colorToMove, false);

        if (moves.isEmpty()) {
            if (isKingInCheck(colorToMove)) {
                return -CHECKMATE_SCORE - depth;
            }
            return 0;
        }

        int bestScore = -INF;

        for (Move move : moves) {
            makeMove(move);
            int score = -plainNegamax(depth - 1, opposite(colorToMove));
            undoMove(move);

            bestScore = Math.max(bestScore, score);
        }

        return bestScore;
    }

    private int alphaBetaNegamax(int depth, int alpha, int beta, int colorToMove) {
        nodesSearched++;

        if (depth == 0) {
            return evaluateFor(colorToMove);
        }

        ArrayList<Move> moves = generateLegalMoves(colorToMove, usesMoveOrdering());

        if (moves.isEmpty()) {
            if (isKingInCheck(colorToMove)) {
                return -CHECKMATE_SCORE - depth;
            }
            return 0;
        }

        int bestScore = -INF;

        for (Move move : moves) {
            makeMove(move);
            int score = -alphaBetaNegamax(depth - 1, -beta, -alpha, opposite(colorToMove));
            undoMove(move);

            bestScore = Math.max(bestScore, score);
            alpha = Math.max(alpha, score);

            if (alpha >= beta) {
                betaCutoffs++;
                break;
            }
        }

        return bestScore;
    }

    private ArrayList<Move> generateLegalMoves(int color) {
        return generateLegalMoves(color, usesMoveOrdering());
    }

    private ArrayList<Move> generateLegalMoves(int color, boolean useMoveOrdering) {
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

                    Move move = new Move(piece, col, row, piece.hittingP);

                    if (GameLayout.castlingP != null) {
                        move.castlingRook = GameLayout.castlingP;
                        move.rookOldCol = move.castlingRook.col;
                        move.rookOldRow = move.castlingRook.row;
                        move.rookOldPreCol = move.castlingRook.preCol;
                        move.rookOldPreRow = move.castlingRook.preRow;
                        move.rookOldX = move.castlingRook.x;
                        move.rookOldY = move.castlingRook.y;
                        move.rookOldMoved = move.castlingRook.moved;

                        if (move.castlingRook.col == 0) {
                            move.rookNewCol = 3;
                        }
                        else if (move.castlingRook.col == 7) {
                            move.rookNewCol = 5;
                        }

                        move.rookNewRow = move.castlingRook.row;
                    }

                    makeMove(move);
                    boolean legal = !isKingInCheck(color);
                    undoMove(move);

                    if (legal) {
                        moves.add(move);
                    }
                }
            }
        }

        if (useMoveOrdering) {
            orderMoves(moves);
        }

        return moves;
    }

    private boolean usesMoveOrdering() {
        return searchMode == SearchMode.ALPHA_BETA_ORDERED;
    }

    private void orderMoves(ArrayList<Move> moves) {
        moves.sort((a, b) -> Integer.compare(getMoveOrderScore(b), getMoveOrderScore(a)));
    }

    private int getMoveOrderScore(Move move) {
        if (move == null) {
            return -INF;
        }

        int score = 0;

        if (move.capturedPiece != null) {
            score += 10_000;
            score += getPieceValue(move.capturedPiece) * 10 - getPieceValue(move.piece);
        }

        if (move.promotedPiece != null) {
            score += 9_000 + getPieceValue(move.promotedPiece);
        }

        if (move.castlingRook != null) {
            score += 850;
        }

        if (isCenterSquare(move.toCol, move.toRow)) {
            score += 350;
        }
        else if (isNearCenterSquare(move.toCol, move.toRow)) {
            score += 140;
        }

        if ((move.piece.type == Type.KNIGHT || move.piece.type == Type.BISHOP)
                && isBackRank(move.piece.color, move.oldRow)
                && !isBackRank(move.piece.color, move.toRow)) {
            score += 260;
        }

        if (move.piece.type == Type.ROOK) {
            if (move.toCol == 3 || move.toCol == 4) {
                score += 220;
            }
            if (move.toRow == 3 || move.toRow == 4) {
                score += 90;
            }
        }

        if ((move.piece.type == Type.KNIGHT || move.piece.type == Type.BISHOP)
                && move.piece.moved) {
            score -= 80;
        }

        if (move.piece.type == Type.QUEEN && move.capturedPiece == null) {
            score -= 120;
        }

        return score;
    }

    private boolean isCenterSquare(int col, int row) {
        return (col == 3 || col == 4) && (row == 3 || row == 4);
    }

    private boolean isNearCenterSquare(int col, int row) {
        return col >= 2 && col <= 5 && row >= 2 && row <= 5;
    }

    private boolean isBackRank(int color, int row) {
        return color == GameLayout.WHITE ? row == 7 : row == 0;
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
        move.pieceIndex = GameLayout.simPieces.indexOf(move.piece);

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
        if (move.piece.type == Type.PAWN) {
            move.piece.twoStepped = Math.abs(move.toRow - move.oldRow) == 2;
        }
        if (move.castlingRook != null) {
            move.castlingRook.col = move.rookNewCol;
            move.castlingRook.row = move.rookNewRow;
            move.castlingRook.x = move.castlingRook.getX(move.rookNewCol);
            move.castlingRook.y = move.castlingRook.getY(move.rookNewRow);
            move.castlingRook.preCol = move.rookNewCol;
            move.castlingRook.preRow = move.rookNewRow;
            move.castlingRook.moved = true;
        }

        if (move.piece.type == Type.PAWN &&
                ((move.piece.color == GameLayout.WHITE && move.toRow == 0) ||
                        (move.piece.color == GameLayout.BLACK && move.toRow == 7))) {
            move.promotedPiece = new Queen(move.piece.color, move.toCol, move.toRow);
            move.promotedPiece.moved = true;
            GameLayout.simPieces.remove(move.piece);
            GameLayout.simPieces.add(move.promotedPiece);
        }
    }

    private void undoMove(Move move) {
        if (move.promotedPiece != null) {
            GameLayout.simPieces.remove(move.promotedPiece);

            if (!GameLayout.simPieces.contains(move.piece)) {
                int insertIndex = Math.min(move.pieceIndex, GameLayout.simPieces.size());
                GameLayout.simPieces.add(insertIndex, move.piece);
            }
        }

        if (move.castlingRook != null) {
            move.castlingRook.col = move.rookOldCol;
            move.castlingRook.row = move.rookOldRow;
            move.castlingRook.preCol = move.rookOldPreCol;
            move.castlingRook.preRow = move.rookOldPreRow;
            move.castlingRook.x = move.rookOldX;
            move.castlingRook.y = move.rookOldY;
            move.castlingRook.moved = move.rookOldMoved;
        }

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

    private void printBenchmark(int depth, Move bestMove) {
        lastSearchNanos = System.nanoTime() - searchStartTime;

        System.out.println("===== AI Benchmark =====");
        System.out.println("Mode: " + searchMode);
        System.out.println("Depth: " + depth);
        System.out.println("Best move: " + formatMove(bestMove));
        System.out.println("Score: " + lastBestScore);
        System.out.println("Legal root moves: " + rootMoves);
        System.out.println("Nodes searched: " + nodesSearched);
        System.out.println("Beta cutoffs: " + betaCutoffs);
        System.out.println("Time: " + String.format("%.2f", getLastSearchMillis()) + " ms");
        System.out.println("Nodes/sec: " + getNodesPerSecond());
        System.out.println("========================");
    }

    private String formatMove(Move move) {
        if (move == null) {
            return "none";
        }

        String moveText = move.piece.type + " " +
                squareName(move.oldCol, move.oldRow) +
                " -> " +
                squareName(move.toCol, move.toRow);

        if (move.capturedPiece != null) {
            moveText += " captures " + move.capturedPiece.type;
        }

        if (move.castlingRook != null) {
            moveText += " castles";
        }

        if (move.promotedPiece != null) {
            moveText += " promotes to " + move.promotedPiece.type;
        }

        return moveText;
    }

    private String squareName(int col, int row) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    private void updateCheckingPiece() {
        game.checkingP = null;

        Piece king = null;

        for (Piece piece : GameLayout.simPieces) {
            if (piece.type == Type.KING && piece.color == game.currentColor) {
                king = piece;
                break;
            }
        }

        if (king == null) {
            return;
        }

        for (Piece piece : GameLayout.simPieces) {
            if (piece.color == game.currentColor) {
                continue;
            }

            GameLayout.castlingP = null;
            piece.hittingP = null;

            if (piece.canMove(king.col, king.row)) {
                game.checkingP = piece;
                return;
            }
        }
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

    private static String colorName(int color) {
        return color == GameLayout.WHITE ? "WHITE" : "BLACK";
    }

    private void syncRealPiecesWithSimPieces() {
        GameLayout.pieces.clear();
        GameLayout.pieces.addAll(GameLayout.simPieces);
    }

    private static class PositionSnapshot {
        private final ArrayList<Piece> piecesSnapshot = new ArrayList<>(GameLayout.pieces);
        private final ArrayList<Piece> simPiecesSnapshot = new ArrayList<>(GameLayout.simPieces);
        private final ArrayList<PieceState> pieceStates = new ArrayList<>();

        private PositionSnapshot() {
            HashSet<Piece> savedPieces = new HashSet<>();

            for (Piece piece : piecesSnapshot) {
                if (savedPieces.add(piece)) {
                    pieceStates.add(new PieceState(piece));
                }
            }

            for (Piece piece : simPiecesSnapshot) {
                if (savedPieces.add(piece)) {
                    pieceStates.add(new PieceState(piece));
                }
            }
        }

        private void restore() {
            for (PieceState pieceState : pieceStates) {
                pieceState.restore();
            }

            GameLayout.pieces.clear();
            GameLayout.pieces.addAll(piecesSnapshot);
            GameLayout.simPieces.clear();
            GameLayout.simPieces.addAll(simPiecesSnapshot);
            GameLayout.castlingP = null;
        }
    }

    private static class PieceState {
        private final Piece piece;
        private final int x;
        private final int y;
        private final int col;
        private final int row;
        private final int preCol;
        private final int preRow;
        private final Piece hittingP;
        private final boolean moved;
        private final boolean twoStepped;

        private PieceState(Piece piece) {
            this.piece = piece;
            this.x = piece.x;
            this.y = piece.y;
            this.col = piece.col;
            this.row = piece.row;
            this.preCol = piece.preCol;
            this.preRow = piece.preRow;
            this.hittingP = piece.hittingP;
            this.moved = piece.moved;
            this.twoStepped = piece.twoStepped;
        }

        private void restore() {
            piece.x = x;
            piece.y = y;
            piece.col = col;
            piece.row = row;
            piece.preCol = preCol;
            piece.preRow = preRow;
            piece.hittingP = hittingP;
            piece.moved = moved;
            piece.twoStepped = twoStepped;
        }
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
        private int pieceIndex = -1;
        private int capturedIndex = -1;

        private Piece castlingRook;
        private int rookOldCol;
        private int rookOldRow;
        private int rookOldPreCol;
        private int rookOldPreRow;
        private int rookOldX;
        private int rookOldY;
        private boolean rookOldMoved;
        private int rookNewCol;
        private int rookNewRow;

        private Piece promotedPiece;

        private Move(Piece piece, int toCol, int toRow, Piece capturedPiece) {
            this.piece = piece;
            this.toCol = toCol;
            this.toRow = toRow;
            this.capturedPiece = capturedPiece;
        }
        public Piece getCapturedPiece() {
            return capturedPiece;
        }
    }
}
