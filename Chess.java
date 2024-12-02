//all chess piece images are from https://greenchess.net/info.php?item=downloads used under CC BY-SA 3.0 (https://creativecommons.org/licenses/by-sa/3.0/deed.en), modified by me.

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Chess {
	public static void main(String[] args) {
		//make the chessboard proportional to the user's screen
		int Screenheight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		ChessBoard chessBoard = new ChessBoard((int) (Screenheight / 1.5));

		// wait 1 second fo the chessboard to have time to be created
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// do nothing
		}

		System.out.println("Put this and the chessboard window side to side on your screen so that the chessboard doesn't disappear every time you type your move");
		
		char playerColor = 'w'; //white always starts
		while (true) { 
			takeTurn(chessBoard, playerColor);
			playerColor = oppositeColor(playerColor);

			if (gameEnd(chessBoard.getSpaces())) {
				break;
			}
		}
	}

	public static char oppositeColor(char color) {
		if (color == 'w') {
			return 'b';
		} else {
			return 'w';
		}
	}

	public static void takeTurn(ChessBoard chessBoard, char playerColor) {
		while (true) {
			Space[][] spaces = chessBoard.getSpaces();

			//if the player's in check, highlight their king's space in red
			int[] kingPos = getKingPos(playerColor, spaces);
			Space kingSpace = spaces[kingPos[0]][kingPos[1]];
			if (isInCheck(playerColor, spaces)) {
				kingSpace.highlightRed();
			}
			chessBoard.drawBoard();

			Space from = askWhatToMove(chessBoard, playerColor);
			Space to = askWhereToMove(chessBoard, from);

			//if the user entered "x" to go back, because they accidentaly chose the wrong piece, restart the turn
			if (to == null) {
				continue;
			}
		
			//make a backup of the piece in the "to" space, for move undoing purposes
			Piece tempPiece = null;
			if (!to.isEmpty()) {
				tempPiece = to.getPiece();
			}

			//reset en passant related variables, so that they're only true for the duration of 1 turn
			resetEnPassantVars(spaces);

			movePiece(spaces, from, to, playerColor);

			//if the chosen move results in a check, undo the move and restart the turn
			if (isInCheck(playerColor, chessBoard.getSpaces())) {
				unmovePiece(spaces, from, to, playerColor);
				to.setPiece(tempPiece);
				System.out.println("Woops. Don't wanna be in check, do you?");
				restoreEnPassantVars(spaces);
				continue;
			}

			kingSpace.unhighlight(); //because the player is no longer in check

			chessBoard.drawBoard(); //so that we see the piece move

			//if a pawn reached the other side of the board, upgrade it
			if (to.hasPiece("pawn") && (to.getLoc()[1] == 0 || to.getLoc()[1] == 7)) {
				upgradePawn(to);
			}

			break;
		}
	}

	public static void upgradePawn(Space to) {
		@SuppressWarnings("resource") //according to stack overflow, you're not supposed to close scanners, but VsCode isn't smart enough to know that
		Scanner reader = new Scanner(System.in);

		System.out.println("Enter Q to upgrade your pawn to a queen, R for rook, K for knight, B for bishop, P for pawn. Anything else for queen");

		String choice = reader.next().toLowerCase();

		switch (choice) { 
			case "p" -> { //do nothing
			}
			case "r" -> to.setPiece(new Rook(to.getPiece().color, to.getPiece().spaces));
			case "k" -> to.setPiece(new Knight(to.getPiece().color, to.getPiece().spaces));
			case "b" -> to.setPiece(new Bishop(to.getPiece().color, to.getPiece().spaces));
			default -> to.setPiece(new Queen(to.getPiece().color, to.getPiece().spaces));
		}
	}

	public static Space askWhatToMove(ChessBoard chessBoard, char playerColor) {
		Space[][] spaces = chessBoard.getSpaces();

		//show the coordinates of the moveable spaces
		ArrayList<int[]> moveablePieceLocs = getMoveablePieceLocs(playerColor, spaces);
		for (int[] moveablePieceLoc : moveablePieceLocs) {
			spaces[moveablePieceLoc[0]][moveablePieceLoc[1]].showCoordinates();
		}
		chessBoard.drawBoard();

		System.out.println("Enter the coordinates of the piece you want to move. Example: a1");

		@SuppressWarnings("resource") //according to stack overflow, you're not supposed to close scanners, but VsCode isn't smart enough to know that
		Scanner reader = new Scanner(System.in);

		//loop until the player chooses a valid space
		while (true) { 
			String input = reader.next().toLowerCase();

			if (!isValidCoordinate(input)) {
				System.out.println("Not a valid coordinate");
				continue;
			}

			int[] locChoice = toComputerUnderstandableCoordinates(input);
			Space spaceChoice = spaces[locChoice[0]][locChoice[1]];

			if (spaceChoice.hasMoveablePiece(playerColor))
			{
				//hide the coordinates of the moveable spaces
				for (int i = 0; i < moveablePieceLocs.size(); i++) {
					spaces[moveablePieceLocs.get(i)[0]][moveablePieceLocs.get(i)[1]].hideCoordinates();
				}

				return spaceChoice;
			} else {
				System.out.println("You can't move that");
			}
		}
	}

	public static Space askWhereToMove(ChessBoard chessBoard, Space from) {
		Space[][] spaces = chessBoard.getSpaces();

		//highlight the spaces with legal moves
		ArrayList<int[]> legalMoves = from.legalMoves();
		for (int i = 0; i < legalMoves.size(); i++) {
			spaces[legalMoves.get(i)[0]][legalMoves.get(i)[1]].highlight();
		}
		chessBoard.drawBoard();

		@SuppressWarnings("resource") //according to stack overflow, you're not supposed to close scanners, but VsCode isn't smart enough to know that
		Scanner reader = new Scanner(System.in);
		System.out.println("Enter the coordinates of where you want to move. Enter \"x\" to choose another piece to move.");

		while (true) { //loop until the player chooses a valid move
			String input = reader.next().toLowerCase();

			if (input.equals("x")) {
				//unhighlight the legal move spaces
				for (int i = 0; i < legalMoves.size(); i++) {
					spaces[legalMoves.get(i)[0]][legalMoves.get(i)[1]].unhighlight();
				}
				chessBoard.drawBoard();

				return null;
			}

			if (isValidCoordinate(input)) {
				int[] locChoice = toComputerUnderstandableCoordinates(input);
				Space spaceChoice = spaces[locChoice[0]][locChoice[1]];

				if (isLegal(spaceChoice, legalMoves, spaces)) {
					//unhighlight the legal move spaces
					for (int i = 0; i < legalMoves.size(); i++) {
						spaces[legalMoves.get(i)[0]][legalMoves.get(i)[1]].unhighlight();
					}
					chessBoard.drawBoard();

					return spaceChoice;
				}

				System.out.println("Not a legal move. Try again.");
			} else {
				System.out.println("Not a valid coordinate. Try again");
			}
		}
	}

	public static boolean isLegal(Space moveChoice, ArrayList<int[]> legalMoves, Space[][] spaces) {
		//loop through every legal move
		for (int[] legalMove : legalMoves) {
			if (moveChoice == spaces[legalMove[0]][legalMove[1]]) {
			return true;
			}
		}
		return false;
	}

	public static boolean isValidCoordinate(String coordinate) {
		return (coordinate.length() == 2 &&
				coordinate.charAt(0) >= 'a' && coordinate.charAt(0) <= 'h' &&
				coordinate.charAt(1) >= '1' && coordinate.charAt(1) <= '8');
	}

	//converts coordinates inputted by the user into coordinates readable by the code
	public static int[] toComputerUnderstandableCoordinates(String coordinates) {
		char letter = coordinates.charAt(0);
		int column = letter - 97;

		int row = coordinates.charAt(1) - 49;

		int[] newCoordinates = {column, row};
		return newCoordinates;
	}  

	public static void movePiece(Space[][] spaces, Space from, Space to, char playerColor) {
		
		if (playerDoingEnPassant(from, to, spaces)) {
			killEnPassantStyle(from, to, spaces);
		}

		//check if a pawn is moving 2 spaces (for en passant purposes)
		if (playerMovingPawn2Spaces(from, to, spaces)) {
			from.getPiece().pawnMoved2SpacesLastMove = true;
		}

		//the to space only ever has a king or rook of the same color as the player if the player is castling
		if (to.hasPieceOfColor(playerColor)) {
			if (to.hasPiece("king")) {
				castle(from, to, spaces);
			} else if (to.hasPiece("rook")) {
				castle(to, from, spaces);
			}
		} else { // if the player isn't castling
			movePieceWithoutAccountingForSpecialCases(from, to);
		}
	}

	public static boolean playerMovingPawn2Spaces(Space from, Space to, Space[][] spaces) {
		if (from.hasPiece("pawn")) {
			try {
				Space twoSpacesInFrontOfPawn = spaces[from.getLoc()[0]][from.getLoc()[1] + 2*from.getPiece().oneForward];
				if (to == twoSpacesInFrontOfPawn) {
					return true;
				}
			} catch (IndexOutOfBoundsException e) { //if there is no space 2 spaces in front of the pawn
				return false;
			}
		}
		
		return false;
	}

	public static boolean playerDoingEnPassant(Space from, Space to, Space[][] spaces) {
		//if a pawn is moving diagonaly into an empty space, that means it's doing en passant
		return from.hasPiece("pawn") && to.isEmpty() && 
		(movingDiagonalyInDirection("left", from, to, spaces) || movingDiagonalyInDirection("right", from, to, spaces));
	}

	public static boolean movingDiagonalyInDirection(String direction, Space from, Space to, Space[][] spaces) {
		try {
			Space diagonal;
			if (direction.equals("left")) {
				diagonal = spaces[from.getLoc()[0] - 1][from.getLoc()[1] + from.getPiece().oneForward];
			} else {
				diagonal = spaces[from.getLoc()[0] - 1][from.getLoc()[1] + from.getPiece().oneForward];
			}
			return to == diagonal;
		} catch (IndexOutOfBoundsException e) { //if there is no space diagonally in the specified direction
			//do nothing
		}

		return false;
	}

	public static void killEnPassantStyle(Space from, Space to, Space[][] spaces) {
		Space spaceWithPawnToKill = spaces[to.getLoc()[0]][to.getLoc()[1] - from.getPiece().oneForward];
		spaceWithPawnToKill.setPiece(null);
		spaceWithPawnToKill.setPawnJustDiedFromEnPassantHere(true);
	}

	public static void resetEnPassantVars(Space[][] spaces) {
		//set pawnMoved2SpacesLastMove and pawnJustDiedFromEnPassantHere for all pieces in rows 3 and 4 (because that's where you go after moving 2 spaces and the
		//only place you can die from en passant) to false, so that they're only true for the duration of 1 turn
		resetEnPassantVarsInRow(spaces, 3);
		resetEnPassantVarsInRow(spaces, 4);
	}

	//loop through every space in a row and reset it's en passant related variables
	public static void resetEnPassantVarsInRow(Space[][] spaces, int row) {
		for (Space[] column : spaces) {
			Space space = column[row];

			space.setBackupPawnJustDiedFromEnPassantHere(space.getPawnJustDiedFromEnPassantHere()); //keep backups in case a turn is undone and 
			//the variables have to be restored
			space.setPawnJustDiedFromEnPassantHere(false);

			if (!space.isEmpty()) {
				space.getPiece().backupPawnMoved2SpacesLastMove = space.getPiece().pawnMoved2SpacesLastMove;
				space.getPiece().pawnMoved2SpacesLastMove = false;
			}
		}
	}

	public static void restoreEnPassantVars(Space[][] spaces) {
		restoreEnPassantVarsInRow(spaces, 3);
		restoreEnPassantVarsInRow(spaces, 4);
	}

	//loop through every space in a row and restore it's en passant related variables to their backups
	public static void restoreEnPassantVarsInRow(Space[][] spaces, int row) {
		for (Space[] column : spaces) {
			Space space = column[row];

			space.setPawnJustDiedFromEnPassantHere(space.getBackupPawnJustDiedFromEnPassantHere());
			if (!space.isEmpty()) {
				space.getPiece().pawnMoved2SpacesLastMove = space.getPiece().backupPawnMoved2SpacesLastMove;
			}
		}
	}

	public static void unmovePiece(Space[][] spaces, Space cameFrom, Space wentTo, char playerColor) {
		//if we're unmoving a pawn
		if (wentTo.hasPiece("pawn")) {
			//bring back the pawns to it's left and right that may have died from en passant
			revivePotentialEnPassantVictims(spaces, cameFrom, playerColor);

			//if it moved 2 spaces
			try {
				Space twoSpacesInFrontOfCameFrom = spaces[cameFrom.getLoc()[0]][cameFrom.getLoc()[1] + 2*wentTo.getPiece().oneForward]; 
				if (twoSpacesInFrontOfCameFrom.equals(wentTo)) {
					wentTo.getPiece().pawnMoved2SpacesLastMove = false;
				}
			} catch (IndexOutOfBoundsException e) { //if there is no space two spaces in front
				//do nothing
			}
		}

		//the wentTo is only ever empty if a castling was done, because the piece doesn't actually move to the space the player clicked during castling
		if (wentTo.isEmpty()) {
			uncastle(spaces, cameFrom, wentTo);
		} else {
			unmovePieceWithoutAccountingForSpecialCases(cameFrom, wentTo);
		}
	}

	public static void revivePotentialEnPassantVictims(Space[][] spaces, Space murderousPawnCameFrom, char playerColor) {
		try {
			Space leftOfMurderousPawnCameFrom = spaces[murderousPawnCameFrom.getLoc()[0] - 1][murderousPawnCameFrom.getLoc()[1]];
			revivePotentialEnPassantVictim(spaces, leftOfMurderousPawnCameFrom, playerColor);   
		} catch (IndexOutOfBoundsException e) { //if there is no space to the left of the pawn
			//do nothing
		}

		try {
			Space rightOfMurderousPawnCameFrom = spaces[murderousPawnCameFrom.getLoc()[0] + 1][murderousPawnCameFrom.getLoc()[1]];
			revivePotentialEnPassantVictim(spaces, rightOfMurderousPawnCameFrom, playerColor);
		} catch (IndexOutOfBoundsException e) { //if there is no space to the right of the pawn
			//do nothing
		}
	}
	
	public static void revivePotentialEnPassantVictim(Space[][] spaces, Space space, char playerColor) {
		if (space.getPawnJustDiedFromEnPassantHere()) {

			if (playerColor == 'w') {
				space.setPiece(new Pawn('b', spaces));
			} else {
				space.setPiece(new Pawn('w', spaces));
			}

			//any pawn that just died from en passant necessarily moved 2 spaces last move
			space.getPiece().pawnMoved2SpacesLastMove = true;
			space.getPiece().backupPawnMoved2SpacesLastMove = true;
			space.getPiece().hasMoved = true;

			space.setPawnJustDiedFromEnPassantHere(false);
			space.setBackupPawnJustDiedFromEnPassantHere(false);
		}
	}

	public static void castle(Space rookFrom, Space kingFrom, Space[][] spaces) {
		int row = rookFrom.getLoc()[1];
		Space rookTo;
		Space kingTo;

		//if the rook is on the left
		if (rookFrom.getLoc()[0] == 0) {
			rookTo = spaces[3][row];
			kingTo = spaces[2][row];
		} else { //if the rook is on the right
			rookTo = spaces[5][row];
			kingTo = spaces[6][row];
		}

		movePieceWithoutAccountingForSpecialCases(rookFrom, rookTo);
		movePieceWithoutAccountingForSpecialCases(kingFrom, kingTo);
	}

	public static void uncastle(Space[][] spaces, Space cameFrom, Space wentTo) {
		int row = cameFrom.getLoc()[1]; // this is the row the castling happened on
		Space kingWentTo;
		Space kingCameFrom = spaces[4][row]; //the king always start in the same column
		Space rookWentTo;
		Space rookCameFrom;

		//if the player clicked on the left rook when asked where to move or when asked what to move.
		//Meaning, if the player castled to the left
		if (wentTo.getLoc()[0] == 0 || cameFrom.getLoc()[0] == 0) {
			kingWentTo = spaces[2][row];
			rookWentTo = spaces[3][row];
			rookCameFrom = spaces[0][row];
		} else { //if the player castled to the right
			kingWentTo = spaces[6][row];
			rookWentTo = spaces[5][row];
			rookCameFrom = spaces[7][row];
		}

		unmovePieceWithoutAccountingForSpecialCases(kingCameFrom, kingWentTo);
		unmovePieceWithoutAccountingForSpecialCases(rookCameFrom, rookWentTo);
	}
	
	public static void movePieceWithoutAccountingForSpecialCases(Space from, Space to) {
		to.setPiece(from.getPiece());
		from.setPiece(null);
		//make sure we know that the piece has moved, for pawns moving 2 spaces and castling purposes
		to.getPiece().backupHasMoved = to.getPiece().hasMoved; //for move undoing purposes
		to.getPiece().hasMoved = true;
	}

	public static void unmovePieceWithoutAccountingForSpecialCases(Space cameFrom, Space wentTo) {
		cameFrom.setPiece(wentTo.getPiece());
		wentTo.setPiece(null);
		//set hasMoved to what it was before the last move, for pawns moving 2 spaces and castling purposes
		cameFrom.getPiece().hasMoved = cameFrom.getPiece().backupHasMoved;
	}

	public static boolean isInCheck(char playerColor, Space[][] spaces) {
		ArrayList<int[]> dangerousLocs = new ArrayList<>();

		//loop through all the spaces and find all the dangerous locations
		for (Space[] column : spaces) {
			for (Space space : column) {
				//if the space contains an opponent's piece, all the spaces it can attack are dangerous
				if (space.hasPieceOfDifferentColor(playerColor)) {
					if (space.hasPiece("pawn")) {
						//If the space has a pawn, only consider diagonal attacking moves as dangerous
						dangerousLocs.addAll(space.diagonalsInFront());
					} else {
						dangerousLocs.addAll(space.legalMoves());
					}
				}
			}
		}
		//if the king is in a dangerous loc, return true
		int[] kingPos = getKingPos(playerColor, spaces);
		for (int[] dangerousLoc : dangerousLocs) {
			if (Arrays.equals(dangerousLoc, kingPos))
			{
				return true;
			}
		}

		return false;
	}

	public static int[] getKingPos(char color, Space[][] spaces) {
		//loop through every space
		for (Space[] column : spaces) {
			for (Space space : column) {
				//if the space has a king of the right color, return it's position
				if (space.hasPiece("king") && space.hasPieceOfColor(color)) {
					return space.getLoc();
				}
			}
		}
		return null;
	}

	public static boolean gameEnd(Space[][] spaces) {
		if (isInCheck('b', spaces) && allMovesLeadToCheck('b', spaces)) {
			System.out.println("Checkmate. White wins!");
			return true;
		}
		if (isInCheck('w', spaces) && allMovesLeadToCheck('w', spaces)) {
			System.out.println("Checkmate. Black wins!");
			return true;
		}
		if ((!isInCheck('b', spaces) && allMovesLeadToCheck('b', spaces)) ||
			(!isInCheck('w', spaces) && allMovesLeadToCheck('w', spaces))) {
			System.out.println("It's a tie!");
			return true;
		}

		return false;
	}

	public static boolean allMovesLeadToCheck(char color, Space[][] spaces) {
		//loop through every moveable piece
		ArrayList<int[]> moveablePieceLocs = getMoveablePieceLocs(color, spaces);
		for (int[] moveablePieceLoc : moveablePieceLocs) {

			Space from = spaces[moveablePieceLoc[0]][moveablePieceLoc[1]];

			//do all of the piece's legal moves
			for (int[] legalMove : from.legalMoves()) {
				Space to = spaces[legalMove[0]][legalMove[1]];

				//make a backup of the piece in the "to" space, for move undoing purposes
				Piece tempPiece = null;
				if (!to.isEmpty()) {
					tempPiece = to.getPiece();
				}

				movePiece(spaces, from, to, color);

				//if the move doesn't result in check, we know there's at least one move that will allow you to escape check
				if (!isInCheck(color, spaces)) {
					//undo the move
					unmovePiece(spaces, from, to, color);
					to.setPiece(tempPiece);

					return false;
				}

				//undo the move
				unmovePiece(spaces, from, to, color);
				to.setPiece(tempPiece);
			}
		}

		return true;
	}

	public static ArrayList<int[]> getMoveablePieceLocs(char playerColor, Space[][] spaces) {
		ArrayList<int[]> moveablePieceLocs = new ArrayList<>();

		//loop through all the spaces
		for (Space[] column : spaces) {
			for (Space space : column) {
				if (space.hasMoveablePiece(playerColor)) {
					moveablePieceLocs.add(space.getLoc());
				}
			}
		}
		return moveablePieceLocs;
	}
}