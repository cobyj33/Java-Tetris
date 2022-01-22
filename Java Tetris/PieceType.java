import java.awt.Color;

public enum PieceType {
    SQUARE(0, Color.YELLOW),
    STRAIGHT(1, new Color(173, 216, 230)), //light blue
    LEFT_L(2, Color.BLUE),
    RIGHT_L(3, new Color(255, 165, 0)), //orange
    T(4, new Color(128, 0, 128)), //purple
    LEFT_Z(5, Color.RED),
    RIGHT_Z(6, Color.GREEN);

    int number;
    Color color;

    PieceType(int number, Color color) {
        this.number = number;
        this.color = color;
    }

    public int getValue() {
        return number;
    }

    public Color getColor() {
        return color;
    }

    public static PieceType getPieceType(int val) {
        for (PieceType p : PieceType.values()) {
            if (p.number == val) {
                return p;
            }
        } return null;
    }

    public static PieceType[] getNextPieces() {

        PieceType[] nextPieces = PieceType.values();
        for (int i = 0; i < nextPieces.length / 2; i++) {
            int toSwitch = (int) (Math.random() * nextPieces.length);
            PieceType temp = nextPieces[i];
            nextPieces[i] = nextPieces[toSwitch];
            nextPieces[toSwitch] = temp;
        }
        //Test Pieces
        //nextPieces = new PieceType[] {PieceType.STRAIGHT, PieceType.STRAIGHT, PieceType.STRAIGHT, PieceType.STRAIGHT, PieceType.STRAIGHT, PieceType.STRAIGHT, PieceType.STRAIGHT};
        //nextPieces = new PieceType[] {PieceType.T};
        return nextPieces;
    }

}