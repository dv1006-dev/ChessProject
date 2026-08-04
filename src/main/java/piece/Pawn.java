package piece;

import org.example.GameLayout;
import org.example.Type;

public class Pawn extends Piece{
    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;

        if (color == GameLayout.WHITE){
            image = getImage("/piece/w-pawn");
        }
        else{
            image = getImage("/piece/b-pawn");
        }

    }
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol,targetRow) && isSameSquare(targetCol,targetRow) == false){
            int moveNum;
            if(color == GameLayout.WHITE){
                moveNum = -1;
            }
            else{
                moveNum =1;
            }

            hittingP = getHitting(targetCol,targetRow);
            if(targetCol == preCol && targetRow == preRow + moveNum && hittingP == null){
                return true;
            }
            if(targetCol == preCol && targetRow == preRow + moveNum*2 && hittingP == null && moved == false &&
                    isSameLine(targetCol,targetRow) == false){
                return true;
            }
            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveNum && hittingP != null && hittingP.color != color){
                return true;
            }

            if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveNum){
                for(Piece piece : GameLayout.simPieces){
                    if(piece.col == targetCol && piece.row == preRow && piece.twoStepped == true){
                        hittingP = piece;
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
