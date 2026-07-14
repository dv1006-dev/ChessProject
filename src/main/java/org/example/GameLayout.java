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

    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();

    public static final int WHITE =0;
    public static final int BLACK =1;
    int currentColor = WHITE;

    public GameLayout() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.LIGHT_GRAY);

        setPieces();
        copyPieces(pieces, simPieces);
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
        pieces.add(new King(WHITE,3,7));
        pieces.add(new Queen(WHITE,4,7));

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

    }
    public void paintComponent(Graphics c){
        super.paintComponent(c);
        Graphics2D c2 = (Graphics2D)c;
        board.draw(c2);

        for(Piece p : simPieces){
            p.draw(c2);

        }
    }



    @Override
    public void run() {
        double drawIntval = 1000000000.0 /FPS;
        double delta = 0;
        long lastT = System.nanoTime();
        long currentT;

        while(game != null){
            currentT = System.nanoTime();

            delta += (currentT - lastT)/drawIntval;
            lastT = currentT;

            if(delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }
}

