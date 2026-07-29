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

