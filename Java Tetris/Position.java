public class Position {
    private int row;
    private int col;

    Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean equals(Position p) {
        if (p.getRow() == row && p.getCol() == col) {
            return true;
        } return false;
    }
    public void print() { System.out.println("Position Data:   Row = " + row + " Col = " + col); }

    public void move(int rowOffset, int colOffset) {
        row += rowOffset;
        col += colOffset;
    }
}