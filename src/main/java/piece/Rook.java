package piece;

import org.example.GameLayout;

public class Rook extends Piece{
    public Rook(int color, int col, int row) {
        super(color, col, row);

        if (color == GameLayout.WHITE) {
            image = getImage("/piece/w-rook");
        } else {
            image = getImage("/piece/b-rook");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol,targetRow) == false){
            if(targetCol == preCol || targetRow == preRow){
                if(isValidSquare(targetCol, targetRow) && isSameLine(targetCol,targetRow) == false){
                    return true;
                }

            }
        }
        return false;
    }
}
