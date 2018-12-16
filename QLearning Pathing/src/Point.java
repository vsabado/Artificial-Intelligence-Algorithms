public class Point {

    private int row = 0;
    private int column = 0;

    public Point(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public Point(Point pt){
        this.row = pt.row;
        this.column = pt.column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Point doAction(Action action){
        int newRow = row;
        int newColumn = column;
        switch(action){
            case up: newRow--; break;
            case down: newRow++; break;
            case right: newColumn++; break;
            case left: newColumn--; break;
            default: break;
        }
        return new Point(newRow, newColumn);
    }

}
