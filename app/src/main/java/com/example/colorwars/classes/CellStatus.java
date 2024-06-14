package com.example.colorwars.classes;

import com.example.colorwars.R;

public class CellStatus {
    private final int[] redDots = {R.drawable.r1, R.drawable.r2, R.drawable.r3, R.drawable.r4};
    private final int[] blueDots = {R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4};
    public static final int MAX_DOT = 4;
    private COLOR color;
    private int dotCount;
    public final int rowIndex, colIndex;

    public CellStatus(int rowIndex, int colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.color = COLOR.NONE;
        this.dotCount = 0;
    }

    public void setColor(COLOR color) {
        this.color = color;
    }
    public COLOR getColor(){
        return color;
    }


    public boolean shouldSpread(){
        return dotCount == MAX_DOT;
    }

    public int makeBlankAndGetImage(){
        color = COLOR.NONE;
        dotCount = 0;
        return R.drawable.box1;
    }

    public int getImage(){
        if(dotCount == 0){
            return R.drawable.box1;
        }
        return (color == COLOR.RED) ? redDots[dotCount-1] : blueDots[dotCount-1];
    }

    public void increaseDot(){
        dotCount = Math.min(dotCount+1,MAX_DOT);
    }

    public boolean canClick(boolean redTurn){
        return redTurn ? (color == COLOR.RED) : (color == COLOR.BLUE);
    }

    public boolean isBlank(){
        return color == COLOR.NONE;
    }

    public enum COLOR{ RED, BLUE, NONE }
    // Add these methods to manage dot count
    public void setDotCount(int dotCount) {
        this.dotCount = dotCount;
    }

    public int getDotCount() {
        return dotCount;
    }
}