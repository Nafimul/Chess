import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class Space {
	private final Color defaultColor;
	private Color color;
	private final int x; //for drawing
	private final int y; //for drawing
	private final int size;
	private Piece piece;
	private boolean shouldDrawCoordinates;
	private final int[] loc;
	private boolean pawnJustDiedFromEnPassantHere = false;
	private boolean backupPawnJustDiedFromEnPassantHere = false;

	public Space(int[] loc, Color defaultColor, int size, int spaceForLabelsAroundBoard) {
		this.size = size;
		x = loc[0]*size + spaceForLabelsAroundBoard;
		y = (7 - loc[1])*size + spaceForLabelsAroundBoard; //this calculation is weird because the first row is the bottom row
		this.defaultColor = defaultColor;
		color = defaultColor;
		this.loc = loc;
	}

	public int[] getLoc() {
		return loc;
	}

	public void setPawnJustDiedFromEnPassantHere(boolean bool) {
		pawnJustDiedFromEnPassantHere = bool;
	}

	public boolean getPawnJustDiedFromEnPassantHere() {
		return pawnJustDiedFromEnPassantHere;
	}
	
	public boolean getBackupPawnJustDiedFromEnPassantHere() {
		return backupPawnJustDiedFromEnPassantHere;
	}

	public void setBackupPawnJustDiedFromEnPassantHere(boolean bool) {
		backupPawnJustDiedFromEnPassantHere = bool;
	}

	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, size, size);
		drawPiece(g);
		if (shouldDrawCoordinates) {
			drawCoordinates(g);
		}

		//drawVars(g); //only for debugging purposes
	}

	private void drawCoordinates(Graphics g) {
		Font font = new Font("TimesRoman", Font.PLAIN, size/7); // size/7 is used to make the letter size relative to the space size. 7 is an arbitrary number
		g.setFont(font);
		g.setColor(oppositeColor(defaultColor));
		g.drawString(userUnderstandableCoordinates(), x + size/20, y + font.getSize() + size/20); //20 is an arbitrary number
	}

	private Color oppositeColor(Color color) {
		if (color == Color.WHITE) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}

	public String userUnderstandableCoordinates() {
		char newRow = (char) (loc[0] + 65);
		char newColumn = (char) (loc[1] + 49);

		return "" + newRow + newColumn;
	}

	private void drawPiece(Graphics g) {
		if (piece != null) {
			Image image = new ImageIcon(piece.imageFile).getImage();
			g.drawImage(image, x, y, size, size, null);
		}
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	public Piece getPiece() {
		return piece;
	}

	public boolean isEmpty() {
		return piece == null;
	}

	public void highlight() {
		if (defaultColor == Color.BLACK)
		{
			color = new Color(6, 79, 2); //dark green
		} else {
			color = new Color(105, 245, 138); //light green
		}
		showCoordinates();
	}

	public void unhighlight() {
		color = defaultColor;
		hideCoordinates();
	}

	public void highlightRed() {
		color = Color.RED;
	}

	public void showCoordinates() {
		shouldDrawCoordinates = true;
	}

	public void hideCoordinates() {
		shouldDrawCoordinates = false;
	}

	public ArrayList<int[]> legalMoves() {
		return piece.legalMoves(loc);
	}

	public ArrayList<int[]> diagonalsInFront() {
		if (!isEmpty()) {
			ArrayList<int[]> diagonalsInFront = new ArrayList<>();

			int[] posDiagonalLeft = new int[] {loc[0] + 1, loc[1] + piece.oneForward};
			diagonalsInFront.add(posDiagonalLeft);

			int[] posDiagonalRight = new int[] {loc[0] - 1, loc[1] + piece.oneForward};
			diagonalsInFront.add(posDiagonalRight);

			return diagonalsInFront;
		}
		return null;
	}

	public boolean hasPiece(String pieceName) {
		if (isEmpty()) {
			return false;
		}
		return getPiece().type.equals(pieceName);
	}

	public boolean hasPieceOfColor(char color) {
		if (isEmpty()) {
			return false;
		}
		return getPiece().color == color;
	}

	public boolean hasPieceOfDifferentColor(char color) {
		if (isEmpty()) {
			return false;
		}  
		return getPiece().color != color;
	}

	public boolean hasMoveablePiece(char playerColor) {
		if (hasPieceOfColor(playerColor)) {
			return !legalMoves().isEmpty();
		}
		return false;
	}

	/*
	//only for debugging
	private void drawVars(Graphics g) {
		Font font = new Font("TimesRoman", Font.PLAIN, size/7); // size/7 is used to make the letter size relative to the space size. 7 is an arbitrary number
		g.setFont(font);
		if (defaultColor.equals(Color.WHITE))
		{
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.WHITE);
		}

		if (pawnJustDiedFromEnPassantHere) {
			g.drawString("pawnJustDiedFromEnPassantHere", x + size/20, y + font.getSize()*3 + size/20);
		}

		if (backupPawnJustDiedFromEnPassantHere) {
			g.drawString("backupPawnJustDiedFromEnPassantHere", x + size/20, y + font.getSize()*3 + size/20);
		}

		try {
			if (piece.pawnMoved2SpacesLastMove) {
				g.drawString("pawnMoved2SpacesLastMove", x + size/20, y + font.getSize()*3 + size/20);
			}
	
			if (piece.backupPawnMoved2SpacesLastMove) {
				g.drawString("backupPawnMoved2SpacesLastMove", x + size/20, y + font.getSize()*3 + size/20);
			}
		} catch (NullPointerException e) {
			//do nothing
		}
	}
	*/
}