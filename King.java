import java.util.ArrayList;

public class King extends Piece {
	
	public King (char color, Space[][] spaces) {
		type = "king";
		setUpVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {
		ArrayList<int[]> legalMoves = new ArrayList<>();

		//possible ways for a king to move
		int[][] positionsToCheck = {{loc[0] + 1, loc[1]}, {loc[0] - 1, loc[1]},
									{loc[0], loc[1] + 1}, {loc[0], loc[1] - 1},
									{loc[0] + 1, loc[1] + 1}, {loc[0] + 1, loc[1] - 1},
									{loc[0] - 1, loc[1] + 1}, {loc[0] - 1, loc[1] - 1}};

		//check if each postion is legal
		for (int[] posToCheck : positionsToCheck) {
			try {
				Space spaceToCheck = spaces[posToCheck[0]][posToCheck[1]];

				if (!spaceToCheck.hasPieceOfColor(color)) {
					legalMoves.add(posToCheck);
				}
			} catch (IndexOutOfBoundsException e) { //if the space to check doesn't exist
				//do nothing
			}
		}

		//check if castling is legal
		addCastlingIfLegal("left", legalMoves, loc[1]);
		addCastlingIfLegal("right", legalMoves, loc[1]);		   

		return legalMoves;
	}

	private void addCastlingIfLegal(String direction, ArrayList<int[]> legalMoves, int row) {
		if (!hasMoved) {
			int[] rookStartingLoc;
			Space[] spacesBetweenRookAndKing;

			if (direction.equals("left")) {
				rookStartingLoc = new int[] {0, row};
				spacesBetweenRookAndKing = new Space[] {spaces[1][row], spaces[2][row], spaces[3][row]};
			} else {
				rookStartingLoc = new int[] {7, row};
				spacesBetweenRookAndKing = new Space[] {spaces[5][row], spaces[6][row]};
			}

			//check if all the spaces between rook and king are empty
			for (Space space : spacesBetweenRookAndKing) {
				if (!space.isEmpty()) {
					return;
				}
			}

			Space rookStartingSpace = spaces[rookStartingLoc[0]][rookStartingLoc[1]];
			//if the rook is in it's starting space and hasn't moved
			if (rookStartingSpace.hasPiece("rook")) {
				if (!rookStartingSpace.getPiece().hasMoved) {
					legalMoves.add(rookStartingLoc); //if the player decides to move to the location of their own rook, the movePiece function in the chess class will
					//automaticaly cause the player to castle
				}
			}
		}
	}
}