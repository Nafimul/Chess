import java.util.ArrayList;

public class Queen extends Piece {
	
	public Queen (char color, Space[][] spaces) {
		type = "queen";
		setupGeneralPieceVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {

		ArrayList<int[]> legalMoves = new ArrayList<>();

		addLegalMovesInDirection("up", legalMoves, loc);
		addLegalMovesInDirection("down", legalMoves, loc);
		addLegalMovesInDirection("left", legalMoves, loc);
		addLegalMovesInDirection("right", legalMoves, loc);
		addLegalMovesInDirection("up-right", legalMoves, loc);
		addLegalMovesInDirection("up-left", legalMoves, loc);
		addLegalMovesInDirection("down-right", legalMoves, loc);
		addLegalMovesInDirection("down-left", legalMoves, loc);

		return legalMoves;

	}
}