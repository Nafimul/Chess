import java.util.ArrayList;

public class Bishop extends Piece {
	
	public Bishop(char color, Space[][] spaces) {
		type = "bishop";
		setupGeneralPieceVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {

		ArrayList<int[]> legalMoves = new ArrayList<>();

		addLegalMovesInDirection("up-right", legalMoves, loc);
		addLegalMovesInDirection("up-left", legalMoves, loc);
		addLegalMovesInDirection("down-right", legalMoves, loc);
		addLegalMovesInDirection("down-left", legalMoves, loc);
		
		return legalMoves;
	}
}