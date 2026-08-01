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
            if(moved == false){
                if(targetCol == preCol+2 && targetRow == preRow && isSameLine(targetCol,targetRow) == false){
                    for(Piece piece : GameLayout.simPieces){
                        if(piece.col == preCol+3 && piece.row == preRow && piece.moved == false){
                            GameLayout.castlingP = piece;
                            return true;
                        }
                    }
                }

                if(targetCol == preCol-2 && targetRow == preRow && isSameLine(targetCol,targetRow) == false){
                    Piece p[] = new Piece[2];
                    for(Piece piece : GameLayout.simPieces){
                        if(piece.col == preCol-3 && piece.row == targetRow){
                            p[0] = piece;
                        }
                         if(piece.col == preCol-4 && piece.row == targetRow){
                            p[1] = piece;
                        }
                        if(p[0] == null && p[1] != null && p[1].moved == false) {
                            GameLayout.castlingP = p[1];
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
