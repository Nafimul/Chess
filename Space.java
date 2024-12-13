import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Space extends JButton {
	private final Color defaultColor;
	private Color color;
	private final int size;
	private Piece piece;
	private final int[] loc;
	private boolean pawnJustDiedFromEnPassantHere = false;
	private boolean backupPawnJustDiedFromEnPassantHere = false;
	private final int[] locPlayerClicked;

	public Space(int[] loc, Color defaultColor, int size, int[] locPlayerClicked) {
		setPreferredSize(new Dimension(size, size));
		this.size = size;
		this.defaultColor = defaultColor;
		color = defaultColor;
		this.loc = loc;
		this.locPlayerClicked = locPlayerClicked;
		addActionListener(new spaceListener()); //to detect if this space was clicked
	}

	private class spaceListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			locPlayerClicked[0] = loc[0];
			locPlayerClicked[1] = loc[1];
			//this tells the chess class that the player clicked a space
			synchronized (locPlayerClicked) {
				locPlayerClicked.notify();
			}
		}
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

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(color);
		g.fillRect(0, 0, size, size);

		if (piece != null) {
			Image image = new ImageIcon(piece.imageFile).getImage();
			g.drawImage(image, 0, 0, size, size, null);
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

	public void highlightGreen() {
		if (defaultColor == Color.BLACK)
		{
			color = new Color(6, 79, 2); //dark green
		} else {
			color = new Color(105, 245, 138); //light green
		}
	}

	public void highlightBlue() {
		if (defaultColor == Color.BLACK)
		{
			color = new Color(1, 33, 38); //dark blue
		} else {
			color = new Color(218, 243, 247); //light blue
		}
	}

	public void unhighlight() {
		color = defaultColor;
	}

	public void highlightRed() {
		color = Color.RED;
	}

	public ArrayList<int[]> legalMoves() {
		return piece.legalMoves(loc);
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
}