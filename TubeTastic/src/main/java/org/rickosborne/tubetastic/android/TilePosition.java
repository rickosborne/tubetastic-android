package org.rickosborne.tubetastic.android;

public class TilePosition {

    protected int colNum;
    protected int rowNum;

    public TilePosition(int colNum, int rowNum) {
        setColNum(colNum);
        setRowNum(rowNum);
    }

    public static String makeId(int colNum, int rowNum) {
        return colNum + "," + rowNum;
    }

    public String getId() {
        return makeId(colNum, rowNum);
    }

    public int getColNum() { return colNum; }
    public int getRowNum() { return rowNum; }
    public void setColNum(int colNum) { this.colNum = colNum; }
    public void setRowNum(int colNum) { this.rowNum = rowNum; }

}
