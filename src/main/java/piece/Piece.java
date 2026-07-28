package piece;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.example.Board;
import org.example.GameLayout;

import javax.imageio.ImageIO;

public class Piece {
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingP;

    public Piece(int color, int col, int row ) {
        this.col = col;
        this.row = row;
        this.color = color;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }
    public BufferedImage getImage(String imagePath){
        BufferedImage image = null;
        try{
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
        } catch(IOException e){
            e.printStackTrace();
        }
        return image;
    }
    public int getX(int col){
        return col* Board.SQUARE_SIZE;
    }
    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    public int getIndex(){
        for(int index =0; index < GameLayout.simPieces.size(); index++){
            if(GameLayout.simPieces.get(index) == this){
                return index;
            }
        }
        return 0;
    }
    public void updatePosition(){
        x = getX(col);
        y = getY(row);
        preCol = getCol(x);
        preRow = getRow(y);
    }
    public void resetPosition(){
        row = preRow;
        col = preCol;
        x = getX(col);
        y = getY(row);
    }
    public boolean canMove(int targetCol, int targetRow){
        return false;
    }
    public boolean isWithinBoard(int targetCol, int targetRow){
        if(targetRow >= 0 && targetRow <= 7 && targetCol >= 0 && targetCol <= 7){
            return true;
        }
        return false;
    }
    public boolean isSameSquare(int targetCol, int targetRow){
        if(targetCol == preCol && targetRow == preRow){
            return true;
        }
        return false;
    }
    public Piece getHitting(int targetCol, int targetRow){
        for(Piece piece : GameLayout.simPieces){
            if(piece.col == targetCol && piece.row == targetRow && piece != this){
                return piece;
            }

        }
        return null;
    }
    public boolean isValidSquare(int targetCo1, int targetRow){
        hittingP = getHitting(targetCo1, targetRow);
        if(hittingP == null){
            return true;
        }
        else{
            if(hittingP.color != this.color){
                return true;
            }
            else{
                hittingP = null;
            }
        }
        return false;
    }
    public boolean isSameLine(int targetCol, int targetRow){
        for(int a = preCol -1; a > targetCol; a--){
            for(Piece piece: GameLayout.simPieces){
                if(piece.col == a && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }

        for(int a = preCol +1; a < targetCol; a++){
            for(Piece piece: GameLayout.simPieces){
                if(piece.col == a && piece.row == targetRow){
                    hittingP = piece;
                    return true;
                }
            }
        }

        for(int r = preRow -1; r > targetRow; r--){
            for(Piece piece: GameLayout.simPieces){
                if(piece.col == targetCol && piece.row == r){
                    hittingP = piece;
                    return true;
                }
            }
        }

        for(int r = preRow +1; r < targetRow; r++){
            for(Piece piece: GameLayout.simPieces){
                if(piece.col == targetCol && piece.row == r){
                    hittingP = piece;
                    return true;
                }
            }
        }

        return false;
    }
    public void draw(Graphics2D c2){

        c2.drawImage(image,x,y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }


}
