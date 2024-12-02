import java.util.ArrayList;

public class Rook extends Piece {
	
	public Rook(char color, Space[][] spaces) {
		type = "rook";
		setUpVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {
		ArrayList<int[]> legalMoves = new ArrayList<>();

		addLegalMovesInDirection("up", legalMoves, loc);
		addLegalMovesInDirection("down", legalMoves, loc);
		addLegalMovesInDirection("left", legalMoves, loc);
		addLegalMovesInDirection("right", legalMoves, loc);
		
		//check if castling is legal
		if (!hasMoved) {
			int row = loc[1];
			int[] kingStartingLoc = new int[] {4, row};
			Space kingStartingSpace = spaces[kingStartingLoc[0]][row];
			Space[] spacesBetweenRookAndKing;

			if (loc[0] == 0) { //the rook is on the left
				spacesBetweenRookAndKing = new Space[] {spaces[1][row], spaces[2][row], spaces[3][row]};
			} else { //the rook is on the right 
				spacesBetweenRookAndKing = new Space[] {spaces[6][row], spaces[5][row]};
			}

			//check if all the spaces between rook and king are empty
			for (Space space : spacesBetweenRookAndKing) {
				if (!space.isEmpty()) {
					return legalMoves;
				}
			}

			//check if the king is in it's starting space and hasn't moved
			if (kingStartingSpace.hasPiece("king")) {
				if (!kingStartingSpace.getPiece().hasMoved) {
					legalMoves.add(kingStartingLoc); //if the player decides to move to the location of a king, the movePiece function in the chess class will
					//automaticaly cause the player to castle
				}
			}
		}
		return legalMoves;
	}
}