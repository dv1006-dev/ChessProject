package org.example;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        GameLayout gl = new GameLayout();
        window.add(gl);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gl.launchGame();

    }

}
