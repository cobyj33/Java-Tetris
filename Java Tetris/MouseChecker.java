import java.awt.event.*;

public class MouseChecker extends MouseAdapter {
	TetrisGame game;
	
	MouseChecker(TetrisGame game) {
		this.game = game;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		game.mousePressed(e);
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		game.mouseReleased(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		game.mouseMoved(e);
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		game.mouseMoved(e);
	}
	
	
}
