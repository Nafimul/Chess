import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;

public class ChessBoard extends Canvas {
	private static final int NUM_ROWS = 8;
	private static final int NUM_COLUMNS = 8;
	private final Space[][] spaces = new Space[NUM_ROWS][NUM_COLUMNS];
	private final JFrame FRAME = new JFrame("Chess Board");
	private final int size;
	private final int spaceSize;
	private final int spaceForLabelsAroundBoard;

	public ChessBoard(int size) {
		this.size = size;
		spaceSize = (int) (this.size / (NUM_ROWS + 0.25));
		spaceForLabelsAroundBoard = spaceSize/4;

		Color color = Color.BLACK; //the first space will be black
		//fill a matrix with space objects that have positions and colors
		for (int row = 0; row < spaces.length; row++) {
			for (int col = 0; col < spaces[row].length; col++) {
				spaces[col][row] = new Space(new int[] {col, row}, color, spaceSize, spaceForLabelsAroundBoard);

				color = oppositeColor(color);
			}
			//change the color again after every row is made, so that the next row starts with the right color
			color = oppositeColor(color);
		}

		AddPiecesToStartingPos('b', 7);
		AddPiecesToStartingPos('w', 0);
		
		setupFrame();
	}

	private Color oppositeColor(Color color) {
		if (color == Color.WHITE) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}

	private void setupFrame() {
		FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FRAME.setResizable(false);
		setSize(size, size);
		FRAME.add(this);
		FRAME.pack();
		FRAME.setVisible(true);
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
		for (int i = 0; i < NUM_COLUMNS; i++) {
			spaces[i][row].setPiece(new Pawn(color, spaces));
		}
	}

	@Override
	public void paint(Graphics g) {
		//draw the letters above the board
		String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H"};
		for (int i = 0; i < NUM_COLUMNS; i++) {
			g.drawString(letters[i], i*spaceSize + spaceForLabelsAroundBoard + spaceSize/2, spaceForLabelsAroundBoard - 2);
			// for the y coordinate, I substracted 2 so that the letters aren't touching the spaces
		}

		//draw the numbers to the left of the board
		String[] numbers = {"8", "7", "6", "5", "4", "3", "2", "1"};
		for (int i = 0; i < NUM_COLUMNS; i++) {
			g.drawString(numbers[i], spaceForLabelsAroundBoard/2 , i*spaceSize + spaceForLabelsAroundBoard + spaceSize/2);
		}

		//draw every space
		for (Space[] column : spaces) {
			for (Space space : column) {
				space.draw(g);
			}
		}
	}

	public void drawBoard() {
		FRAME.add(this); //this calls the paint method
	}

	public Space[][] getSpaces() {
		return spaces;
	}
}