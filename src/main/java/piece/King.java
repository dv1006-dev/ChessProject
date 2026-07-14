package piece;

import org.example.GameLayout;

public class King extends Piece{
    public King(int color, int col, int row){
        super(color, col, row);

            if(color == GameLayout.WHITE){
                image = getImage("/piece/w-king");
            }
            else{
                image = getImage("/piece/b-king");
            }

    }
}
