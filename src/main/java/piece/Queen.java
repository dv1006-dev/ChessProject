package piece;

import org.example.GameLayout;

public class Queen extends Piece{
    public Queen(int color, int col, int row){
        super(color, col, row);

            if(color == GameLayout.WHITE){
                    image = getImage("/piece/w-queen");
            }
            else{
                    image = getImage("/piece/b-queen");
            }

    }
}

