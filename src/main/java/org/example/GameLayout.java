package org.example;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import java.awt.*;

public class GameLayout extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 1100;
    Thread game;
    final int FPS = 60;
    Board board = new Board();

    public GameLayout() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.LIGHT_GRAY);

    }
    public void launchGame(){
        game = new Thread(this);
        game.start();
    }


    private void update(){

    }
    public void paintComponent(Graphics c){
        super.paintComponent(c);
        Graphics2D c2 = (Graphics2D)c;
        board.draw(c2);
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

