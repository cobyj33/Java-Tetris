import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;


@SuppressWarnings("serial")
public class TetrisGame extends JPanel {

    final int rows = 20;
    final int cols = 12;
    int squareSize;
    int gameWidth;
    int gameHeight;
    int score;

    int defaultRow = 1;
    int middle = cols / 2;
    int turns;
    boolean placed;
    boolean inGame;
    boolean canSave;
    PieceType nextPieceType;
    PieceType[] nextPieces;
    PieceType savedPieceType;

    Piece currentPiece;
    Piece shadowPiece;
    Color shadowColor;
    
    Position mousePosition;
    ArrayList<Piece> placedPieces;
    boolean canControl;

    Color[] backgroundColors;
    Dimension size;
    KeyChecker keychecker;
    MouseChecker mousechecker;
    SideBar sideBar;
    private final int padding = 10;
    Timer timer;
    int delay;

    TetrisGame() {
        backgroundColors = new Color[] {new Color(0, 0, 0), new Color(100, 100, 100)};
        placedPieces = new ArrayList<>();
        currentPiece = null;
        shadowPiece = null;
        
        keychecker = new KeyChecker(this);
        addKeyListener(keychecker);
        mousechecker = new MouseChecker(this);
        addMouseListener(mousechecker);
        addMouseMotionListener(mousechecker);
        
        
        setFocusable(true);
        requestFocus(true);
        
        Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        squareSize = Math.min(screenDimensions.width, screenDimensions.height) / Math.max(rows, cols) * 4 / 5;
        gameWidth = cols * squareSize;
        gameHeight = rows * squareSize;
        sideBar = new SideBar();
        sideBar.setSize(gameWidth / 3, gameHeight);
        sideBar.setPreferredSize(new Dimension(gameWidth / 3, gameHeight));
        setSize(gameWidth, gameHeight);
        //sideBar.setVisible(false);
        //setVisible(false);
    }

    public int getPadding() { return padding; }

    public void start() {
        inGame = true;
        backgroundColors = new Color[] {new Color(0, 0, 0), new Color(100, 100, 100)};

        if (getParent().getLayout() == null) {
            setBounds(padding, padding, gameWidth, gameHeight);
            sideBar.setBounds(gameWidth + (2 * padding), padding, gameWidth / 3, gameHeight);
        }
        setBackground(Color.BLACK);

        turns = 0;
        score = 0;
        placedPieces.clear();
        nextPieces = PieceType.getNextPieces();
        nextPieceType = nextPieces[turns % nextPieces.length];
        currentPiece = Piece.createPiece(nextPieceType, defaultRow, middle);
        turns++;
        nextPieceType = nextPieces[turns % nextPieces.length];
        createShadow();
        placed = false;
        canControl = true;
        canSave = true;
        delay = 200;
        timer = new Timer(delay, e -> iterateGame());
        timer.start();
    }

    public void end() {
        inGame = false;
        timer.stop();
        int response = JOptionPane.showConfirmDialog(this, "Would you like to replay?");
        if (response == 0) {
            start();
        } else {
            System.exit(0);
        }
    }

    public void iterateGame() {
        if (!placed) {
            placed = checkPlaced(currentPiece);
        }

        if (placed) {
            turns++;
            canControl = false;
            placedPieces.add(currentPiece);
            checkLine();
            timer.setDelay(delay);
            currentPiece = Piece.createPiece(nextPieceType, defaultRow, middle);
            if (turns % nextPieces.length == 0) { nextPieces = PieceType.getNextPieces(); }
            nextPieceType = nextPieces[turns % nextPieces.length];
            createShadow();
            canSave = true;
            placed = false;
            repaint();
        } else {
            canControl = true;
            currentPiece.move(1, 0);
            repaintCurrentPiece();
        }

        sideBar.repaintSideBar();
    }

    public void repaintCurrentPiece() {
        int shadowPieceLeft = shadowPiece.getLeft();
        int shadowPieceTop = shadowPiece.getTop();
        shadowPiece = null;
        repaint((squareSize) * shadowPieceLeft - squareSize, (squareSize) * shadowPieceTop - squareSize, 5 * squareSize, 5 * squareSize);
        createShadow();
        repaint((squareSize) * currentPiece.getLeft() - squareSize, (squareSize) * currentPiece.getTop() - (3 * squareSize), 5 * squareSize, 6 * squareSize);
        repaint((squareSize) * shadowPiece.getLeft() - squareSize, (squareSize) * shadowPiece.getTop() - squareSize, 5 * squareSize, 5 * squareSize);
    }

    public boolean checkPlaced(Piece piece) {
        Position[] positions = piece.getPositions();
        int furthestLeft = piece.getLeft();
        int furthestRight = piece.getRight();

        if (piece.getBottom() >= rows - 1) { //if it has hit the bottom
            return true;
        }

        Position[] placedPositions = placedPieces.stream()
        		.parallel()
                .map(p -> p.getPositions())
                .flatMap(pos -> Arrays.stream(pos).parallel().filter(x -> x.getCol() >= furthestLeft && x.getCol() <= furthestRight))
                .toArray(Position[]::new);




        Position[] underPiece = Arrays.stream(positions)
        		.parallel()
                .map(x -> new Position(x.getRow() + 1, x.getCol()))
                .toArray(Position[]::new);


        for (int i = 0; i < placedPositions.length; i++) {
            for (int j = 0; j < underPiece.length; j++) {
                if (underPiece[j].equals(placedPositions[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    private void createShadow() {
        shadowPiece = Piece.createShadowPiece(currentPiece, shadowColor);
        boolean blocked = checkPlaced(shadowPiece);

        while (!blocked) {
            shadowPiece.move(1, 0);
            blocked = checkPlaced(shadowPiece);
        }

    }

    private void checkLine() {

        int[] placedRows = placedPieces.stream() //gets all positions of placed rows
        		.parallel()
                .map(p -> p.getPositions())
                .flatMap(pos -> Arrays.stream(pos))
                .mapToInt(Position::getRow)
                .toArray();

        ArrayList<Integer> rowsToDelete = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            int row = i;
            long countInRow = Arrays.stream(placedRows)
            		.parallel()
                    .filter(x -> x == row)
                    .count();
            if (countInRow >= cols) { //row found
                if (row <= defaultRow) {
                    end(); return;
                }
                rowsToDelete.add(row);
            }
        }

        for (int i = 0; i < rowsToDelete.size(); i++) {
            clearRow(rowsToDelete.get(i));
            System.out.println("row deleted");
        }

    }

    private void clearRow(int row) {
        score++;
        delay -= 2;
        for (int i = 0; i < placedPieces.size(); i++) {
            Piece current = placedPieces.get(i); //goes through every piece
            Position[] positions = current.getPositions(); //gets the positions of that piece

            for (int pos = 0; pos < positions.length; pos++) {
                if (positions[pos].getRow() == row) { //if the position of the piece is in that row, it removes it
                    current.removePosition(pos);
                    positions = current.getPositions();
                    if (positions.length == 0) { //if the piece has no positions, it can be removed as it is useless
                        placedPieces.remove(i);
                        i--;
                        break;
                    }
                    pos--;
                    System.out.println();
                } else if (positions[pos].getRow() < row) {
                    positions[pos].setPos(positions[pos].getRow() + 1, positions[pos].getCol());
                }
            }
        }
    }

    public boolean checkRotatable(Piece piece) {
        Piece possiblePiece = Piece.copyOf(piece);
        Position[] possiblePositions = possiblePiece.getPositions();
        int left = possiblePiece.getLeft();
        int right = possiblePiece.getRight();
        possiblePiece.rotate(rows, cols);
        Position[] bannedPositions = placedPieces.stream()
                .map(Piece::getPositions)
                .flatMap(Arrays::stream)
                .parallel()
                .filter(x -> x.getCol() >= left && x.getCol() <= right)
                .toArray(Position[]::new);
        
        if (!checkOverlap(possiblePositions, bannedPositions)) { return true; }

        System.out.println("Rock Bottom");
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
        	for (int colOffset = -1; colOffset <= 1; colOffset++) {
        		if (rowOffset == 0 && colOffset == 0) { continue; }
        		Piece current = Piece.copyOf(possiblePiece);
        		current.move(rowOffset, colOffset);
        		possiblePositions = current.getPositions();
        		if (!checkOverlap(possiblePositions, bannedPositions)) {
        			currentPiece.move(rowOffset, colOffset);
        			return true;
        		}
        	}
        }
        
        return false;
        
    }

    private boolean canMove(int rowOffset, int colOffset) {
        Piece possiblePiece = Piece.copyOf(currentPiece);
        possiblePiece.move(rowOffset, colOffset);
        int left = possiblePiece.getLeft();
        int right = possiblePiece.getRight();
        Position[] bannedPositions = placedPieces.stream()
                .map(Piece::getPositions)
                .flatMap(Arrays::stream)
                .parallel()
                .filter(x -> x.getCol() >= left && x.getCol() <= right)
                .toArray(Position[]::new);
        return !checkOverlap(possiblePiece.getPositions(), bannedPositions);
    }

    private boolean checkOverlap(Position[] possiblePositions, Position[] bannedPositions) {
    	if (isOutOfBounds(possiblePositions)) { return true; }

    	for (int i = 0; i < possiblePositions.length; i++) {
    		Position current = possiblePositions[i];
    		int matches = (int) Arrays.stream(bannedPositions)
    				.parallel()
    				.filter(x -> x.equals(current))
    				.count();
    		if (matches > 0) { return true; }
    	}
        return false;
    }
    
    @SuppressWarnings("unused")
	private boolean isOutOfBounds(Piece piece) {
    	return isOutOfBounds(piece.getPositions());
    }
    
    private boolean isOutOfBounds(Position[] positions) {
    	for (int i = 0; i < positions.length; i++) {
    		Position current = positions[i];
    		int currentRow = current.getRow();
    		int currentCol = current.getCol();
    		if (currentRow >= rows || currentRow < 0 || currentCol >= cols || currentCol < 0) { return true; }
    	}
    	return false;
    }



    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        super.paintComponent(g);
        int bouncer = 0;
        for (int row = 0; row < rows; row++) {
            bouncer++;
            for (int col = 0; col < cols; col++) {
                g2D.setPaint(backgroundColors[bouncer % 2]); bouncer++;
                g2D.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
            }
        }

        if (currentPiece != null) {
            paintPiece(currentPiece, g2D, squareSize);
            if (shadowPiece != null) { paintPiece(shadowPiece, g2D, squareSize); }
        }
        
        placedPieces.stream().forEach(piece -> paintPiece(piece, g2D, squareSize));

        if (!inGame) {
            g2D.setFont(new Font("Times New Roman", Font.BOLD, 50));
            g2D.setPaint(Color.WHITE);
            FontMetrics metrics = getFontMetrics(g2D.getFont());
            g2D.drawString("GAME OVER", (getWidth() - metrics.stringWidth("GAME OVER")) / 2, getHeight() / 2);
        }

    }

    private void paintPiece(Piece piece, Graphics2D g2D, int squareSize) {
        Position[] positions = piece.getPositions();
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == null) { continue; }
            Position current = positions[i];
            g2D.setPaint(piece.getColor());
            g2D.fillRect(current.getCol() * squareSize, current.getRow() * squareSize, squareSize, squareSize);
            g2D.setPaint(Color.BLACK);
            g2D.drawRect(current.getCol() * squareSize, current.getRow() * squareSize, squareSize, squareSize);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (inGame) {
            int keyCode = e.getKeyCode();
            boolean moved = false;
            if (keyCode == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            } else if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) { //move piece left
                if (currentPiece.getLeft() <= 0) { return; }
                if (canControl && canMove(0, -1)) {
                    currentPiece.move(0, -1);
                    moved = true;
                }
            } else if (keyCode == KeyEvent.VK_D|| keyCode == KeyEvent.VK_RIGHT) { //move piece right
                if (currentPiece.getRight() >= cols - 1) { return; }
                if (canControl && canMove(0, 1)) {
                    currentPiece.move(0, 1);
                    moved = true;
                }
            } else if (keyCode == KeyEvent.VK_SPACE) { //place piece down
                currentPiece.setPositions(shadowPiece.getPositions());
                placed = true;
                canControl = false;
            } else if (keyCode == KeyEvent.VK_R) {
                if (canControl) {
                    if (checkRotatable(currentPiece)) {
                        currentPiece.rotate(rows, cols);
                        moved = true;
                    }
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                savePiece();
            }

            if (moved) {
                repaintCurrentPiece();
            }
        }
        else { System.out.println("invalid"); }
    }
    
    public void savePiece() {
    	if (canSave) {
            if (savedPieceType == null) {
                savedPieceType = currentPiece.getPieceType();
                currentPiece = Piece.createPiece(nextPieceType, defaultRow, middle);
            } else {
                PieceType temp = currentPiece.getPieceType();
                currentPiece = Piece.createPiece(savedPieceType, defaultRow, middle);
                savedPieceType = temp;
            }
            canSave = false;
            turns++;
            nextPieceType = nextPieces[turns % nextPieces.length];
            repaintCurrentPiece();
        }
    }
    
    public void mouseMoved(MouseEvent e) {
    	int currentCol = e.getX() / squareSize;
    	int currentRow = e.getY() / rows;
    	Position newMousePosition = new Position(currentRow, currentCol);
    	
    	if (mousePosition == null) {
    		mousePosition = newMousePosition;
    	}
    	
    	if (mousePosition.getCol() != currentCol) {
    		if (!canControl) { return; }
    		canControl = false;
    		int offset = currentPiece.findCenter().getCol() - currentCol;
    		offset = currentCol - currentPiece.findCenter().getCol();
    		if (canMove(0,  offset)) {
    			currentPiece.move(0, offset);
    		}
    		mousePosition.setPos(currentRow, currentCol);
    		repaintCurrentPiece();
    		canControl = true;
    	}
    }
    
    public void mouseReleased(MouseEvent e) {
    	if (SwingUtilities.isRightMouseButton(e)) {
    		if (canControl) {
                if (checkRotatable(currentPiece)) {
                    currentPiece.rotate(rows, cols);
                }
            }
    	} else if (SwingUtilities.isLeftMouseButton(e))  {
    		shadowColor = null;
    		currentPiece.setPositions(shadowPiece.getPositions());
    		placed = true;
    		canControl = false;
    	} else {
    		savePiece();
    	}
    	
    	repaintCurrentPiece();
    }
    
    public void mousePressed(MouseEvent e) {
    	if (SwingUtilities.isLeftMouseButton(e)) {
    		System.out.println("clicked");
        	shadowColor = new Color(255, 255, 255);
    	}
    }

    class SideBar extends JPanel {
        IndicatorPanel nextUpIndicator;
        IndicatorPanel savedIndicator;
        JLabel scoreDisplay;
        Dimension elementSize;
        Border defaultElementBorder;

        SideBar() {
            defaultElementBorder = BorderFactory.createLineBorder(Color.WHITE, 1);
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            nextUpIndicator = new IndicatorPanel();
            //nextUpIndicator.setBorder(defaultElementBorder);
            setBackground(Color.BLACK);

            savedIndicator = new IndicatorPanel();
            //savedIndicator.setBorder(defaultElementBorder);

            scoreDisplay = new JLabel();
            //scoreDisplay.setBorder(defaultElementBorder);
            scoreDisplay.setFont(new Font("Times New Roman", Font.BOLD, 14));
            scoreDisplay.setHorizontalTextPosition(JLabel.CENTER);
            scoreDisplay.setVerticalTextPosition(JLabel.CENTER);
        }

        public void setSize(int width, int height) {
            int sideLength = (int) (this.getWidth() * 0.75);
            super.setSize(width, height);
            super.setPreferredSize(new Dimension(width, height));
            System.out.println("heyooo");
//            nextUpIndicator.setBounds(0, 0, sideLength, sideLength);
//            scoreDisplay.setBounds(0, sideLength, sideLength, sideLength);
//            savedIndicator.setBounds(0, sideLength * 2, sideLength, sideLength);
            nextUpIndicator.setSize(sideLength, sideLength);
            scoreDisplay.setSize(sideLength, sideLength);
            savedIndicator.setSize(sideLength, sideLength);

            GridBagConstraints c = new GridBagConstraints();

            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1;
            c.insets = new Insets(5, 5, 5, 5);
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;
            c.gridx = 0;
            c.gridy = 0;
            add(nextUpIndicator, c);
            c.gridy = 1;
            add(scoreDisplay, c);
            c.gridy = 2;
            add(savedIndicator, c);
            repaint();
        }

        public void repaintSideBar() {
            scoreDisplay.setText("SCORE: " + score);
            repaint();
            if (savedPieceType != null) {
                savedIndicator.setPiece(Piece.createPiece(savedPieceType, nextUpIndicator.defaultRow, nextUpIndicator.middle));
                savedIndicator.repaint();
            }

            if (!nextUpIndicator.hasPiece() || nextUpIndicator.piece.getPieceType() != nextPieceType) {
                nextUpIndicator.setPiece(Piece.createPiece(nextPieceType, nextUpIndicator.defaultRow, nextUpIndicator.middle));
                nextUpIndicator.repaint();
            }
        }

    }

    class IndicatorPanel extends JPanel {
        int rows;
        int cols;
        int defaultRow;
        int middle;
        int squareSize;
        Piece piece;

        IndicatorPanel() {
            rows = 5;
            cols = 5;
            defaultRow = 2;
            middle = cols / 2;
            setBackground(Color.BLACK);
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }
        public boolean hasPiece() { if (piece == null) { return false; } return true; }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            squareSize = getWidth() / cols;
            Graphics2D g2D = (Graphics2D) g;

            int bouncer = 0;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    g2D.setPaint(backgroundColors[bouncer % 2]); bouncer++;
                    g2D.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
                }
            }
            if (piece != null ) {
                paintPiece(piece, g2D, squareSize);
            }

            g2D.setPaint(Color.WHITE);
            g2D.setStroke(new BasicStroke(5));
            g2D.drawRect(0, 0, rows * squareSize, cols * squareSize);
        }
    }


}
