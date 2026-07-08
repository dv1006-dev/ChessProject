package org.example;

import java.awt.*;

public class Board {
    final int MAX_ROW =8;
    final int MAX_COL =8;
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;

    public void draw(Graphics2D c2){
        int colr =0;
        for(int r =0; r < MAX_ROW; r++ ){
            for(int c =0; c < MAX_COL; c++){
                if(colr == 0){
                    c2.setColor(new Color(119, 0, 255));
                    colr = 1;
                }
                else{
                    c2.setColor(new Color(255, 0, 218));
                    colr=0;
                }
                c2.fillRect(c*SQUARE_SIZE, r*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            if(colr ==0){
                colr =1;
            } else{
                colr =0;
            }
        }
    }
}
