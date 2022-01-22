import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;

public class Piece {
    private Color color;
    private Position[] positions;
    private PieceType pieceType;
    private int left;
    private int right;
    private int top;
    private int bottom;

    Piece(Position[] positions, PieceType type) {
        this.positions = positions;
        //System.out.println(type);
        pieceType = type;
        color = type.getColor();
        setBounds();
    }

    public Color getColor() { return color; }
    public Position[] getPositions() { return positions; }
    public void setPositions(Position[] pos) {
        positions = pos;
    }
    public PieceType getPieceType() { return pieceType; }
    public void setColor(Color color) { this.color = color; }
    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getTop() { return top; }
    public int getBottom() { return bottom; }

    private void setBounds() {
        left = Arrays.stream(positions).mapToInt(pos -> pos.getCol()).min().getAsInt();
        right = Arrays.stream(positions).mapToInt(pos -> pos.getCol()).max().getAsInt();
        bottom = Arrays.stream(positions).mapToInt(pos -> pos.getRow()).max().getAsInt();
        top = Arrays.stream(positions).mapToInt(pos -> pos.getRow()).min().getAsInt();
    }

    public void move(int rowOffset, int colOffset) {
        for (int i = 0; i < positions.length; i++) {
            positions[i].move(rowOffset, colOffset);
        }

        left += colOffset;
        right += colOffset;
        bottom += rowOffset;
        top += rowOffset;
    }

    public void removePosition(int index) {
        Position[] newPositions = new Position[positions.length - 1];
        int drop = 0;
        for (int i = 0; i < positions.length; i++) {
            if (i == index) { drop++; continue; }
            newPositions[i - drop] = positions[i];
        }

        positions = newPositions;
    }
    
    public Position findCenter() { //purely for rotation
    	int averageRow = (top + bottom) / 2;
    	int averageCol = (left + right) / 2;
    	Position possibleCenter = new Position(averageRow, averageCol);
    	Optional<Position> center = Arrays.stream(positions)
    			.parallel()
    			.filter(pos -> pos.equals(possibleCenter))
    			.findFirst();
    	if (center.isPresent()) { return center.get(); }
    	return positions[0];
    }

    public void rotate(int rows, int cols) {
    	
        Position pivot = findCenter();
        pivot = new Position(pivot.getRow(), pivot.getCol());
        for (int i = 0; i < positions.length; i++) {
            int row = positions[i].getRow() - pivot.getRow();
            int col = positions[i].getCol() - pivot.getCol();
            int temp = row;
            row = -col;
            col = temp;

            row += pivot.getRow();
            col += pivot.getCol();

            positions[i].setPos(row, col);
        }
        setBounds();
    }

    public void printPositions() {
        if (positions.length == 0) { System.out.println("No available Positions"); return; }
        Arrays.stream(positions).forEach(Position::print);
    }

    public static Piece createPiece(PieceType pieceType, int defaultRow, int middle) {
        Position[] pos = new Position[4];

        switch (pieceType) {
            case STRAIGHT:
                for (int i = 0; i < pos.length; i++) {
                    pos[i] = new Position(defaultRow, (middle - 2) + i);
                }
                break;
            case T:
                for (int i = 0; i < pos.length; i++) {
                    pos[i] = new Position(defaultRow, (middle - 1) + i);
                }
                pos[3] = new Position(defaultRow - 1, middle);
                break;
            case LEFT_Z:
                pos[0] = new Position(defaultRow - 1, middle - 1);
                for (int i = 1; i < pos.length; i++) {
                    pos[i] = new Position(defaultRow - 1 + (i / 2), middle + (i / 3));
                }
                break;
            case RIGHT_Z:
                pos[0] = new Position(defaultRow, middle - 1);
                for (int i = 1; i < pos.length; i++) {
                    pos[i] = new Position(defaultRow - (i / 2), middle + (i / 3));
                }
                break;
            case LEFT_L:
                for (int i = 0; i < pos.length - 1; i++) {
                    pos[i] = new Position(defaultRow - 1 + i, middle);
                }
                pos[3] = new Position(defaultRow + 1, middle - 1);
                break;
            case RIGHT_L:
                for (int i = 0; i < pos.length - 1; i++) {
                    pos[i] = new Position(defaultRow - 1 + i, middle);
                }
                pos[3] = new Position(defaultRow + 1, middle + 1);
                break;
            case SQUARE:
                pos[0] = new Position(defaultRow, middle);
                pos[1] = new Position(defaultRow - 1, middle);
                pos[2] = new Position(defaultRow - 1, middle + 1);
                pos[3] = new Position(defaultRow, middle + 1);
                break;


        }

        return new Piece(pos, pieceType);
    }

    public static Piece createShadowPiece(Piece piece, Color color) {
        Piece shadow = Piece.copyOf(piece);
        if (color == null) {
        	shadow.setColor(new Color(200, 200, 200));
        } else {
        	shadow.setColor(color);
        }
        return shadow;
    }

    public static Piece copyOf(Piece piece) {
    	Position[] positions = new Position[4];
        Position[] toCopy = piece.getPositions();
        for (int i = 0; i < toCopy.length; i++) {
            int row = toCopy[i].getRow();
            int col = toCopy[i].getCol();
            positions[i] = new Position(row, col);
        }
        

        return new Piece(positions, piece.getPieceType());
    }
}