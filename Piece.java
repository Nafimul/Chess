import java.util.ArrayList;

public abstract class Piece {
	protected String imageFile;
	protected char color;
	protected String type;
	protected Space[][] spaces;
	
	//only relevant for pawns and for castling, but putting these only in those classes
	//is much more complicated and less readable than putting them here
	protected  boolean hasMoved;
	protected boolean backupHasMoved;

	public void setupGeneralPieceVariables(char color, Space[][] spaces) {
		this.spaces = spaces;
		this.color = color;
		if (color == 'w') {
			imageFile = "assets/white-" + type + ".png";
		} else {
			imageFile = "assets/black-" + type + ".png";
		}
	}

	public abstract ArrayList<int[]> legalMoves(int[] loc);

	protected void addLegalMovesInDirection(String direction, ArrayList<int[]> legalMoves, int[] loc) {
		try {
			//Loop through every space in a certain direction. If there are no more spaces in that direction,
			//the catch statement will end the loop with an ArrayIndexOutOfBoundsException.
			for (int i = 1; ; i++) {

				int[] posToCheck;
				posToCheck = switch (direction) {
					case "down" -> new int[] {loc[0], loc[1] - i};
					case "up" -> new int[] {loc[0], loc[1] + i};
					case "left" -> new int[] {loc[0] - i, loc[1]};
					case "right" -> new int[] {loc[0] + i, loc[1]};
					case "up-right" -> new int[] {loc[0] + i, loc[1] + i};
					case "up-left" -> new int[] {loc[0] - i, loc[1] + i};
					case "down-right" -> new int[] {loc[0] + i, loc[1] - i};
					default -> new int[] {loc[0] - i, loc[1] - i}; //default: down-left
				};

				Space spaceToCheck = spaces[posToCheck[0]][posToCheck[1]];
				
				//if the space is empty, add its position to legalMoves
				if (spaceToCheck.isEmpty()) {
					legalMoves.add(posToCheck);
				} 
				//if there's a piece of the same color blocking the way, end the loop
				else if (spaceToCheck.hasPieceOfColor(color)) {
					break;
				} 
				//if an opponent's piece is blocking the way, add its postion to legalMoves, then end the loop
				else {
					legalMoves.add(posToCheck);
					break;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) { 
			//do nothing
		}
	}
}