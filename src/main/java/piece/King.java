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

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol,targetRow)){
            if(Math.abs(targetRow - preRow) + Math.abs(targetCol - preCol ) == 1 ||
                    Math.abs(targetRow - preRow) * Math.abs(targetCol - preCol ) == 1){

                if(isValidSquare(targetCol,targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
