import java.util.ArrayList;

public class Knight extends Piece {
	
	public Knight (char color, Space[][] spaces) {
		type = "knight";
		setupGeneralPieceVariables(color, spaces);
	}

	@Override
	public ArrayList<int[]> legalMoves(int[] loc) {

		ArrayList<int[]> legalMoves = new ArrayList<>();

		//possible ways for a knight to move
		int[][] positionsToCheck = {{loc[0] + 2, loc[1] + 1}, {loc[0] - 2, loc[1] + 1},
									{loc[0] + 2, loc[1] - 1}, {loc[0] - 2, loc[1] - 1}, 
									{loc[0] + 1, loc[1] + 2}, {loc[0] - 1, loc[1] + 2}, 
									{loc[0] + 1, loc[1] - 2}, {loc[0] - 1, loc[1] - 2}};

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

		return legalMoves;
	}
}