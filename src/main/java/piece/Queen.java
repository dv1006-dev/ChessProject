package piece;

import org.example.GameLayout;
import org.example.Type;

public class Queen extends Piece{
    public Queen(int color, int col, int row){
        super(color, col, row);
        type = Type.QUEEN;

            if(color == GameLayout.WHITE){
                    image = getImage("/piece/w-queen");
            }
            else{
                    image = getImage("/piece/b-queen");
            }

    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol,targetRow) == false){
            if(Math.abs(targetCol -preCol) == Math.abs(targetRow - preRow) ){
                if(isValidSquare(targetCol,targetRow) && isSameDiagnolLine(targetCol, targetRow) == false){
                    return true;
                }
            }
            if(targetCol == preCol || targetRow == preRow){
                if(isValidSquare(targetCol, targetRow) && isSameLine(targetCol,targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }
}

