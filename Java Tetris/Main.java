import javax.swing.*;
import java.awt.*;

public class Main {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		JPanel gameHolder = new JPanel();
		gameHolder.setLayout(null);
		gameHolder.setBackground(Color.BLACK);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setTitle("Tetris");
		
		TetrisGame tetris = new TetrisGame();
		TetrisGame.SideBar sideBar = tetris.sideBar;
		int padding = tetris.getPadding();
		
		gameHolder.setPreferredSize(new Dimension(tetris.getWidth() + sideBar.getWidth() + (3 * padding), tetris.getHeight() + (2 * padding)));
		gameHolder.add(tetris);
		gameHolder.add(sideBar);
		frame.setSize(gameHolder.getWidth() * 2, gameHolder.getHeight() * 2);
		frame.add(gameHolder, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setLocation(frame.getLocationOnScreen().x - frame.getWidth() / 2, frame.getLocationOnScreen().y - frame.getHeight() / 2);
		tetris.start();
	}

}
