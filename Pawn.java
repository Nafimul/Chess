import java.util.ArrayList;

public class Pawn extends Piece {
	
	public Pawn(char color, Space[][] spaces) {
		type = "pawn";
		setUpVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {

		ArrayList<int[]> legalMoves = new ArrayList<>();
		
		//check if going forward is legal
		int[] posInFront = new int[] {loc[0], loc[1] + oneForward}; //all postions are from the piece's point of view
		int[] pos2InFront = new int[] {loc[0], loc[1] + 2 * oneForward};
		try {
			Space spaceInFront = spaces[posInFront[0]][posInFront[1]];
			if (spaceInFront.isEmpty()) {
				legalMoves.add(posInFront);
				
				//check if going forward 2 spaces is legal
				Space space2InFront = spaces[pos2InFront[0]][pos2InFront[1]];
				if (!hasMoved && space2InFront.isEmpty()) {
					legalMoves.add(pos2InFront);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) { //if the space to check doesn't exist
			//do nothing
		}


		int[] posDiagonalLeft = new int[] {loc[0] + 1, loc[1] + oneForward};
		int[] posDiagonalRight = new int[] {loc[0] - 1, loc[1] + oneForward};
		
		checkDiagonalLegality(posDiagonalLeft, legalMoves);
		checkDiagonalLegality(posDiagonalRight, legalMoves);

		checkEnPassantLegality(posDiagonalLeft, legalMoves);
		checkEnPassantLegality(posDiagonalRight, legalMoves);

		return legalMoves;
	}

	private void checkEnPassantLegality(int[] diagonalPos, ArrayList<int[]> legalMoves) {
		int[] posToTheSide = new int[] {diagonalPos[0], diagonalPos[1] - oneForward};
		try {
			Space spaceToTheSide = spaces[posToTheSide[0]][posToTheSide[1]];
			if (spaceToTheSide.hasPiece("pawn")) {
				if (spaceToTheSide.getPiece().pawnMoved2SpacesLastMove) {
					legalMoves.add(diagonalPos);
				}
			}	
		} catch (ArrayIndexOutOfBoundsException e) { //if the space to check doesn't exist
			//do nothing
		}
	}

	private void checkDiagonalLegality(int[] diagonalPos, ArrayList<int[]> legalMoves) {
		try {
			Space diagonalSpace = spaces[diagonalPos[0]][diagonalPos[1]];
			if (diagonalSpace.hasPieceOfDifferentColor(color)) {
					legalMoves.add(diagonalPos);
				}	 
		} catch (ArrayIndexOutOfBoundsException e) { //if the space to check doesn't exist
			//do nothing
		}
	}
}