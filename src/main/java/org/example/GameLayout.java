package org.example;
import piece.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;

public class GameLayout extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    Thread game;
    final int FPS = 60;
    Board board = new Board();
    Mouse mouse = new Mouse();

    MinimaxAI ai;
    private ArrayList<MinimaxAI.BenchmarkResult> benchmarkResults = new ArrayList<>();
    private int positionNumber = 0;

    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activep, checkingP;
    public static Piece castlingP;

    public static final int WHITE =0;
    public static final int BLACK =1;
    int currentColor = WHITE;

    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean draw;

    // ADDED
    private int halfmoveClock = 0;

    // ADDED
    private ArrayList<String> positionHistory = new ArrayList<>();


    public GameLayout() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);

        setPieces();
        copyPieces(pieces, simPieces);
        //Temporary Addition
        FENLoader.loadFEN(this, "8/8/8/8/3Q4/8/8/4k2K w - - 0 1");

        ai = new MinimaxAI(this, BLACK);

        addMouseListener(mouse);
        addMouseMotionListener(mouse);

    }
    public void launchGame(){
        game = new Thread(this);
        game.start();
    }

    public void setPieces(){
        //White pieces
        pieces.add(new Pawn(WHITE,0,6));
        pieces.add(new Pawn(WHITE,1,6));
        pieces.add(new Pawn(WHITE,2,6));
        pieces.add(new Pawn(WHITE,3,6));
        pieces.add(new Pawn(WHITE,4,6));
        pieces.add(new Pawn(WHITE,5,6));
        pieces.add(new Pawn(WHITE,6,6));
        pieces.add(new Pawn(WHITE,7,6));
        pieces.add(new Knight(WHITE,1,7));
        pieces.add(new Knight(WHITE,6,7));
        pieces.add(new Rook(WHITE,0,7));
        pieces.add(new Rook(WHITE,7,7));
        pieces.add(new Bishop(WHITE,2,7));
        pieces.add(new Bishop(WHITE,5,7));
        pieces.add(new King(WHITE,4,7));
        pieces.add(new Queen(WHITE,3,7));

        pieces.add(new Pawn(BLACK,0,1));
        pieces.add(new Pawn(BLACK,1,1));
        pieces.add(new Pawn(BLACK,2,1));
        pieces.add(new Pawn(BLACK,3,1));
        pieces.add(new Pawn(BLACK,4,1));
        pieces.add(new Pawn(BLACK,5,1));
        pieces.add(new Pawn(BLACK,6,1));
        pieces.add(new Pawn(BLACK,7,1));
        pieces.add(new Knight(BLACK,1,0));
        pieces.add(new Knight(BLACK,6,0));
        pieces.add(new Rook(BLACK,0,0));
        pieces.add(new Rook(BLACK,7,0));
        pieces.add(new Bishop(BLACK,2,0));
        pieces.add(new Bishop(BLACK,5,0));
        pieces.add(new King(BLACK,4,0));
        pieces.add(new Queen(BLACK,3,0));
    }
    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for(Piece s : source){
            target.add(s);
        }
    }



    private void update(){
        if(promotion){
            promoting();
        }
        else if(gameOver == false && draw == false){
            if(mouse.pressed){
                if(activep == null){
                    for(Piece piece : simPieces){
                        if(piece.color == currentColor &&
                                piece.col == mouse.x/Board.SQUARE_SIZE &&
                                piece.row == mouse.y/Board.SQUARE_SIZE) {
                            activep = piece;
                        }
                    }
                }
                else{
                    simulate();
                }
            }
            if(mouse.pressed == false){
                if(activep != null){
                    if(validSquare) {
                        copyPieces(simPieces, pieces);
                        activep.updatePosition();
                        if(castlingP != null){
                            castlingP.updatePosition();
                        }
                        // ADDED
                        // Pawn moves and captures reset the 50-move counter.
                        if(activep.type == Type.PAWN || activep.hittingP != null) {
                            halfmoveClock = 0;
                        }
                        else {
                            halfmoveClock++;
                        }
                        finishMove(false);
                        if (currentColor == BLACK && !gameOver && !draw && !promotion) {
                            positionNumber++;

                            ArrayList<MinimaxAI.BenchmarkResult> results = MinimaxAI.runBenchmark(this, BLACK, 3, positionNumber);

                            benchmarkResults.addAll(results);
                            MinimaxAI.saveBenchmarkResults(results);

                            MinimaxAI.Move aiMove = ai.makeBestMove(3);

                            if (aiMove != null) {
                                activep = aiMove.piece;
                                if (activep.type == Type.PAWN || aiMove.getCapturedPiece() != null) {
                                    halfmoveClock = 0;
                                }
                                else {
                                    halfmoveClock++;
                                }
                                finishMove(true);
                            }
                        }
                    }
                    else {
                        copyPieces(pieces, simPieces);
                        activep.resetPosition();
                        activep = null;
                    }
                }
            }
        }




    }
    private void simulate(){
        canMove = false;
        validSquare = false;

        copyPieces(pieces, simPieces);
        if(castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        activep.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activep.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activep.col = activep.getCol(activep.x);
        activep.row = activep.getRow(activep.y);

        if(activep.canMove(activep.col, activep.row)){
            canMove = true;

            if(activep.hittingP != null){
                simPieces.remove(activep.hittingP.getIndex());
            }
            checkCastling();
            if(isIllegal(activep) == false && opponentCanCaptureKing() == false){
            validSquare = true;
            }
        }
    }

    private void finishMove(boolean aiMove) {
        if (isKingInCheck() && isCheckMate()) {
            gameOver = true;
            return;
        }

        if (isDraw() && !isKingInCheck()) {
            draw = true;
            return;
        }

        if (!aiMove && canPromote()) {
            promotion = true;
            return;
        }

        changePlayer();
        positionHistory.add(getPositionKey());

        if (isFiftyMoveRule() || isThreefoldRepetition()) {
            draw = true;
        }
    }

    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : GameLayout.simPieces) {
                if (piece.color != king.color && piece != king && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }

        }
        return false;
    }
    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);

        for(Piece piece : GameLayout.simPieces){
            if(piece.color != king.color && piece.canMove(king.col,king.row)){
                return true;
            }
        }
        return false;
    }
    private boolean isKingInCheck(){
        Piece king = getKing(true);
        if(activep.canMove(king.col, king.row) ){
            checkingP = activep;
            return true;
        }else{
            checkingP = null;
        }



        return false;
    }
    private Piece getKing(boolean opponent){
        Piece king = null;

        for(Piece piece: GameLayout.simPieces){
            if(opponent){
                if(piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }else{
                if(piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                }
            }
        }
        return king;
    }
    private boolean isCheckMate(){
        Piece king = getKing(true);

        if(kingCanMove(king)){
            return false;
        }
        else{
            int colDiff = Math.abs(checkingP.col -king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if(colDiff == 0){
                if(checkingP.row < king.row){
                    for(int row = checkingP.row ; row < king.row; row++) {
                        for (Piece piece : GameLayout.simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    for(int row = checkingP.row ; row > king.row; row--) {
                        for (Piece piece : GameLayout.simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
                                return false;
                            }
                        }
                    }
                }
            }
            if(rowDiff == 0){
                if(checkingP.col < king.col){
                    for(int col = checkingP.col ; col < king.col; col++) {
                        for (Piece piece : GameLayout.simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.col > king.col){
                    for(int col = checkingP.col ; col > king.col; col--) {
                        for (Piece piece : GameLayout.simPieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
                                return false;
                            }
                        }
                    }
                }

            }
            else if(rowDiff == colDiff){
                if(checkingP.row < king.row){
                    if(checkingP.col < king.col){
                        for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++){
                            for(Piece piece: GameLayout.simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++){
                            for(Piece piece: GameLayout.simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){
                    if(checkingP.col > king.col){
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--){
                            for(Piece piece: GameLayout.simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col < king.col){
                        for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--){
                            for(Piece piece: GameLayout.simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;

    }
    private boolean kingCanMove(Piece king){
        if(isValidSquare( king, -1,-1)) {return true;}
        if(isValidSquare( king, -1,0)){return true;}
        if(isValidSquare( king, -1,1)){return true;}
        if(isValidSquare( king, 0,-1)){return true;}
        if(isValidSquare( king, 0,1)){return true;}
        if(isValidSquare( king, 1,-1)){return true;}
        if(isValidSquare( king, 1,0)){return true;}
        if(isValidSquare( king, 1,1)){return true;}

        return false;
    }
    private boolean isValidSquare(Piece king, int colPlus, int rowPlus){
        boolean isValidSquare = false;

        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)){
            if(king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if(isIllegal(king) == false){
                isValidSquare = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces,simPieces);

        return isValidSquare;
    }
    private boolean isDraw(){
        if(isStalemate()){
            return true;
        }
        if(isInsufficientMaterial()){
            return true;
        }
        if(isFiftyMoveRule()){
            return true;
        }
        if(isThreefoldRepetition()){
            return true;
        }

        return false;
    }
    // ADDED
    private boolean isFiftyMoveRule() {
        return halfmoveClock >= 100;
    }
    // ADDED
    private boolean isThreefoldRepetition() {

        String currentPosition = getPositionKey();
        int count = 0;

        for(String position : positionHistory) {
            if(position.equals(currentPosition)) {
                count++;
            }
        }

        return count >= 3;
    }
    // ADDED
    private String getPositionKey() {

        StringBuilder position = new StringBuilder();

        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {

                Piece pieceAtSquare = null;

                for(Piece piece : pieces) {
                    if(piece.col == col && piece.row == row) {
                        pieceAtSquare = piece;
                        break;
                    }
                }

                if(pieceAtSquare == null) {
                    position.append(".");
                }
                else {
                    position.append(pieceAtSquare.color);
                    position.append(pieceAtSquare.type);
                }
            }
        }

        // ADDED
        position.append(currentColor);

        return position.toString();
    }
    private boolean isInsufficientMaterial(){
            int bishops = 0;
            int knights = 0;
            int otherPieces = 0;

            int bishopSquareColor = -1;

            for (Piece piece : GameLayout.simPieces) {

                if (piece == null) {
                    continue;
                }

                // Kings do not count as material.
                if (piece.type == Type.KING) {
                    continue;
                }

                if (piece.type == Type.BISHOP) {

                    bishops++;

                    int squareColor = (piece.row + piece.col) % 2;

                    if (bishopSquareColor == -1) {
                        bishopSquareColor = squareColor;
                    } else if (bishopSquareColor != squareColor) {
                        // Bishops are on opposite-colored squares.
                        return false;
                    }

                } else if (piece.type == Type.KNIGHT) {

                    knights++;

                } else {

                    // Pawn, rook, or queen.
                    otherPieces++;
                }
            }

            // King vs King
            if (bishops == 0 && knights == 0 && otherPieces == 0) {
                return true;
            }

            // King + Bishop vs King
            if (bishops == 1 && knights == 0 && otherPieces == 0) {
                return true;
            }

            // King + Knight vs King
            if (bishops == 0 && knights == 1 && otherPieces == 0) {
                return true;
            }

            // King + Bishop vs King + Bishop
            // when both bishops are on the same color.
            if (bishops == 2 && knights == 0 && otherPieces == 0) {
                return true;
            }

            return false;

    }
    private boolean isStalemate(){
        int count = 0;
        for(Piece piece: simPieces){
            if(piece.color != currentColor){
                count++;
            }
        }
        if(count == 1) {
            if (kingCanMove(getKing(true)) == false) {
                return true;
            }
        }
        return false;
        
    }
        private void checkCastling () {
            if (castlingP != null) {
                if (castlingP.col == 0) {
                    castlingP.col += 3;
                } else if (castlingP.col == 7) {
                    castlingP.col -= 2;
                }
                castlingP.x = castlingP.getX(castlingP.col);
            }
        }

        private void changePlayer () {
            if (currentColor == WHITE) {
                currentColor = BLACK;

                for (Piece piece : pieces) {
                    if (piece.color == BLACK) {
                        piece.twoStepped = false;
                    }
                }
            } else {
                currentColor = WHITE;

                for (Piece piece : pieces) {
                    if (piece.color == WHITE) {
                        piece.twoStepped = false;
                    }
                }
            }
            activep = null;
        }

        private boolean canPromote () {
            if (activep.type == Type.PAWN) {
                if (currentColor == WHITE && activep.row == 0 || currentColor == BLACK && activep.row == 7) {
                    promoPieces.clear();
                    promoPieces.add(new Rook(currentColor, 9, 2));
                    promoPieces.add(new Bishop(currentColor, 9, 3));
                    promoPieces.add(new Knight(currentColor, 9, 4));
                    promoPieces.add(new Queen(currentColor, 9, 5));
                    return true;
                }

            }
            return false;
        }

        private void promoting () {
            if (mouse.pressed) {
                for (Piece piece : promoPieces) {
                    if (piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                        switch (piece.type) {
                            case ROOK:
                                simPieces.add(new Rook(currentColor, activep.col, activep.row));
                                break;
                            case QUEEN:
                                simPieces.add(new Queen(currentColor, activep.col, activep.row));
                                break;
                            case BISHOP:
                                simPieces.add(new Bishop(currentColor, activep.col, activep.row));
                                break;
                            case KNIGHT:
                                simPieces.add(new Knight(currentColor, activep.col, activep.row));
                                break;

                            default:
                                break;
                        }
                        simPieces.remove(activep.getIndex());
                        copyPieces(simPieces, pieces);
                        activep = null;
                        promotion = false;
                        // ADDED
                        halfmoveClock = 0;

                        // ADDED
                        changePlayer();

                        // ADDED
                        positionHistory.add(getPositionKey());

                        if (currentColor == BLACK && !gameOver && !draw) {
                            MinimaxAI.Move aiMove = ai.makeBestMove(3);

                            if (aiMove != null) {
                                activep = aiMove.piece;
                                finishMove(true);
                            }
                        }

                        // ADDED
                        if(isFiftyMoveRule() || isThreefoldRepetition()) {
                            draw = true;
                        }
                    }
                }
            }
        }
        public void paintComponent (Graphics c){
            super.paintComponent(c);
            Graphics2D c2 = (Graphics2D) c;
            board.draw(c2);

            for (Piece p : simPieces) {
                p.draw(c2);
            }

            if (activep != null) {
                if (canMove) {
                    if(isIllegal(activep) || opponentCanCaptureKing()){
                        c2.setColor(Color.GRAY);
                        c2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        c2.fillRect(activep.col * Board.SQUARE_SIZE, activep.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                        c2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    } else {
                        c2.setColor(Color.white);
                        c2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        c2.fillRect(activep.col * Board.SQUARE_SIZE, activep.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                        c2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    }
                }

                activep.draw(c2);
            }

            c2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            c2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
            c2.setColor(Color.white);


            if (promotion) {
                c2.drawString("Promote to: ", 840, 150);
                for (Piece piece : promoPieces) {
                    c2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
                }
            }
            else{
                if(currentColor == WHITE){
                    c2.drawString("White's Turn", 840, 550);
                    if(checkingP != null && checkingP.color == BLACK){
                        c2.setColor(Color.RED);
                        c2.drawString("The King is", 840, 650);
                        c2.drawString("in Check!", 840, 700);
                    }
                }
                else{
                    c2.drawString("Black's Turn", 840, 550);
                    if(checkingP != null && checkingP.color == WHITE){
                        c2.setColor(Color.RED);
                        c2.drawString("The King is", 840, 650);
                        c2.drawString("in Check!", 840, 700);
                    }
                }
            }

            if(gameOver){
                String s = "";
                if(currentColor == WHITE){
                    s= "White Won!";
                    c2.setColor(Color.WHITE);
                } else{
                    s = "Black Won!";
                    c2.setColor(Color.BLACK);
                }
                c2.setFont(new Font("Arial", Font.PLAIN, 90));
                c2.drawString(s, 200, 420);

            }
            if(draw){
                c2.setFont(new Font("Arial", Font.PLAIN, 90));
                c2.drawString("Draw!", 200, 420);
            }
        }


        @Override
        public void run () {
            double drawIntval = 1000000000.0 / FPS;
            double delta = 0;
            long lastT = System.nanoTime();
            long currentT;

            while (game != null) {
                currentT = System.nanoTime();

                delta += (currentT - lastT) / drawIntval;
                lastT = currentT;

                if (delta >= 1) {
                    update();
                    repaint();
                    delta--;
                }
            }
        }
    }

