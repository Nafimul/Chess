import java.awt.*;
import javax.swing.*;

public class ChessBoard extends JFrame {
	private static final int NUM_ROWSCOLS = 8;
	private final Space[][] spaces = new Space[NUM_ROWSCOLS][NUM_ROWSCOLS];

	public ChessBoard(int size, int[] locPlayerClicked) {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(NUM_ROWSCOLS, NUM_ROWSCOLS));
		getContentPane().add(BorderLayout.CENTER, centerPanel);

		int spaceSize = (int)(size / NUM_ROWSCOLS);

		Color color = Color.BLACK; //the first space will be black
		//add the spaces
		for (int row = NUM_ROWSCOLS - 1; row >= 0; row--) { //row 1 will be at the bottom, because that's how chess.com does it
			for (int col = 0; col < NUM_ROWSCOLS; col++) {
				Space space = new Space(new int[] {col, row}, color, spaceSize, locPlayerClicked);
				centerPanel.add(space);
				spaces[col][row] = space;
				color = oppositeColor(color);
			}
			//change the color again after every row is made, so that the next row starts with the right color
			color = oppositeColor(color);
		}

		AddPiecesToStartingPos('b', 7);
		AddPiecesToStartingPos('w', 0);

		//add labels around the board

		//add some empty space for alignment purposes
		JPanel northPanel = new JPanel(new BorderLayout());
		JLabel emptyLabelForAlignmentPurposes = new JLabel("   ");
		northPanel.add(BorderLayout.WEST, emptyLabelForAlignmentPurposes);

		//add the column labels
		JPanel colLabelPanel = new JPanel(new GridLayout(1, NUM_ROWSCOLS));
		for (int i = 0; i < NUM_ROWSCOLS; i++) {
			JLabel colLabel = new JLabel(String.valueOf((char)('A' + i)), SwingConstants.CENTER);
			colLabelPanel.add(colLabel);
		}
		northPanel.add(BorderLayout.CENTER, colLabelPanel);
		getContentPane().add(BorderLayout.NORTH, northPanel);
		
		//add the row labels. Row 1 will be at the bottom, because that's how chess.com does it
		JPanel westPanel = new JPanel(new GridLayout(NUM_ROWSCOLS, 1));
		for (int i = 0; i < NUM_ROWSCOLS; i++) {
			JLabel rowLabel = new JLabel(" " + String.valueOf(NUM_ROWSCOLS - i) + " ");
			westPanel.add(rowLabel);
		}
		getContentPane().add(BorderLayout.WEST, westPanel);
		
		//finish setting up the frame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);
	}

	private Color oppositeColor(Color color) {
		if (color == Color.WHITE) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}

	private void AddPiecesToStartingPos(char color, int row) {
		spaces[0][row].setPiece(new Rook(color, spaces));
		spaces[1][row].setPiece(new Knight(color, spaces));
		spaces[2][row].setPiece(new Bishop(color, spaces));
		spaces[3][row].setPiece(new Queen(color, spaces));
		spaces[4][row].setPiece(new King(color, spaces));
		spaces[5][row].setPiece(new Bishop(color, spaces));
		spaces[6][row].setPiece(new Knight(color, spaces));
		spaces[7][row].setPiece(new Rook(color, spaces));
		
		if (color == 'b')
		{
			addPawnsToRow(color, 6);
		} else {
			addPawnsToRow(color, 1);
		}
	}

	private void addPawnsToRow(char color, int row)
	{
		for (int i = 0; i < NUM_ROWSCOLS; i++) {
			spaces[i][row].setPiece(new Pawn(color, spaces));
		}
	}

	public Space[][] getSpaces() {
		return spaces;
	}
}