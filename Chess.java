//all chess piece images are from https://greenchess.net/info.php?item=downloads used under CC BY-SA 3.0, modified by me.

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Chess {
	private final int[] locPlayerClicked = new int[2];
	private final ChessBoard chessBoard;
	private final Space[][] spaces;
	private Space spaceWithUpgradingPawn;
	private final Object upgrade = new Object();
	private final JPanel upgradePanel = new JPanel();
	private final Space[] lastMove = new Space[] {new Space(null, null, 0, null),
													new Space(null, null, 0, null)};

	public static void main(String[] args) {
		new Chess().play();
	}

	public Chess() {
		//make the chessboard proportional to the user's screen
		double Screenheight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		chessBoard = new ChessBoard((int) (Screenheight / 1.2), locPlayerClicked); //1.2 is an arbitrary number

		spaces = chessBoard.getSpaces();
		setupUpgradePanel();
	}

	public void play() {
		char playerColor = 'w'; //white always starts
		while (true) { 
			takeTurn(playerColor);
			playerColor = oppositeColor(playerColor);

			if (gameEnd()) {
				break;
			}
		}
	}

	private char oppositeColor(char color) {
		if (color == 'w') {
			return 'b';
		} else {
			return 'w';
		}
	}

	private void takeTurn(char playerColor) {
		//if the player's in check, highlight their king's space in red
		int[] kingLoc = getKingLoc(playerColor);
		Space kingSpace = spaces[kingLoc[0]][kingLoc[1]];
		if (isInCheck(playerColor)) {
			kingSpace.highlightRed();
		}
		chessBoard.repaint();

		Space from;
		Space to;

		while (true) {
			from = chooseSpace();
			if (from.hasMoveablePiece(playerColor)) {
				break;
			}
		}

		ArrayList<int[]> legalMoves = from.legalMoves();
		highlightLegalMoves(legalMoves);

		while (true) { 
			to = chooseSpace();

			//if the player clicked the same piece twice to undo their first click, restart the turn
			if (to == from) {
				unhighlightLegalMoves(legalMoves);
				takeTurn(playerColor);
				return;
			}

			if (isLegal(to, legalMoves)) {
				unhighlightLegalMoves(legalMoves);
				break;
			}
		}
	
		//make a backup of the piece in the "to" space, for move undoing purposes
		Piece tempPiece = null;
		if (!to.isEmpty()) {
			tempPiece = to.getPiece();
		}

		//reset en passant related variables, so that they're only true for the duration of 1 turn
		resetEnPassantVars();

		lastMove[0].unhighlight();
		lastMove[1].unhighlight();
		movePiece(from, to, playerColor);

		//if the chosen move results in a check
		if (isInCheck(playerColor)) {
			//flash the king in red
			kingLoc = getKingLoc(playerColor);
			kingSpace = spaces[kingLoc[0]][kingLoc[1]];
			kingSpace.highlightRed();
			try {
				Thread.sleep(200);
			} catch (InterruptedException ex) {
				//this will never happen
			}
			kingSpace.unhighlight();

			//undo the move
			unmovePiece(from, to, playerColor);
			to.setPiece(tempPiece);
			restoreEnPassantVars();

			//restart the turn
			takeTurn(playerColor);
			return;
		}

		kingSpace.unhighlight(); //because the player can't possibly be in check here

		from.highlightBlue();
		to.highlightBlue();
		lastMove[0] = from;
		lastMove[1] = to;

		//if a pawn reached the other side of the board, upgrade it
		if (to.hasPiece("pawn") && (to.getLoc()[1] == 0 || to.getLoc()[1] == 7)) {
			upgradePawn(to);
		}
	}

	private Space chooseSpace() {
		try {
			synchronized (locPlayerClicked) {
				locPlayerClicked.wait(); //locPlayerClicked will be notified by an ActionListener in the Space class
				return spaces[locPlayerClicked[0]][locPlayerClicked[1]];
			}
		} catch (InterruptedException e) {
			return spaces[0][0]; //this will never happen, but this function has to return something to compile
		}
	}

	private void upgradePawn(Space space) {
		spaceWithUpgradingPawn = space;

		//show the upgradePanel
		chessBoard.getContentPane().add(BorderLayout.SOUTH, upgradePanel);
		chessBoard.pack(); //so that the upgrade panel fits in the frame
		chessBoard.repaint();

		//wait until the player clicks an upgrade button
		try {
			synchronized (upgrade) {
				upgrade.wait();
			}
		} catch (InterruptedException e) {
			//this will never run
		}

		//hide the upgradePanel
		chessBoard.getContentPane().remove(upgradePanel);
		chessBoard.pack();
	}

	private void setupUpgradePanel() {
		upgradePanel.add(new JLabel("Upgrade your pawn to a:"));

		JButton queen = new JButton("Queen");
		JButton rook = new JButton("Rook");
		JButton knight = new JButton("Knight");
		JButton bishop = new JButton("Bishop");

		queen.addActionListener(new upgradeToQueen());
		rook.addActionListener(new upgradeToRook());
		knight.addActionListener(new upgradeToKnight());
		bishop.addActionListener(new upgradeToBishop());

		upgradePanel.add(queen);
		upgradePanel.add(rook);
		upgradePanel.add(knight);
		upgradePanel.add(bishop);
	}

	private class upgradeToQueen implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			spaceWithUpgradingPawn.setPiece(new Queen(spaceWithUpgradingPawn.getPiece().color, spaces));
			synchronized (upgrade) {
				upgrade.notify();
			}
		}
	}

	private class upgradeToRook implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			spaceWithUpgradingPawn.setPiece(new Rook(spaceWithUpgradingPawn.getPiece().color, spaces));
			((Rook)spaceWithUpgradingPawn.getPiece()).hasMoved = true; //this only matters for rooks
			synchronized (upgrade) {
				upgrade.notify();
			}
		}
	}

	private class upgradeToKnight implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			spaceWithUpgradingPawn.setPiece(new Knight(spaceWithUpgradingPawn.getPiece().color, spaces));
			synchronized (upgrade) {
				upgrade.notify();
			}
		}
	}

	private class upgradeToBishop implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			spaceWithUpgradingPawn.setPiece(new Bishop(spaceWithUpgradingPawn.getPiece().color, spaces));
			synchronized (upgrade) {
				upgrade.notify();
			}
		}
	}

	private void highlightLegalMoves(ArrayList<int[]> legalMoves) {
		for (int i = 0; i < legalMoves.size(); i++) {
			spaces[legalMoves.get(i)[0]][legalMoves.get(i)[1]].highlightGreen();
		}
		chessBoard.repaint();
	}

	private void unhighlightLegalMoves(ArrayList<int[]> legalMoves) {
		for (int i = 0; i < legalMoves.size(); i++) {
			spaces[legalMoves.get(i)[0]][legalMoves.get(i)[1]].unhighlight();
		}
		chessBoard.repaint();
	}

	private boolean isLegal(Space moveChoice, ArrayList<int[]> legalMoves) {
		//loop through every legal move
		for (int[] legalMove : legalMoves) {
			if (moveChoice == spaces[legalMove[0]][legalMove[1]]) {
			return true;
			}
		}
		return false;
	}

	private void movePiece(Space from, Space to, char playerColor) {
		if (playerDoingEnPassant(from, to)) {
			killEnPassantStyle(from, to);
		}

		//check if a pawn is moving 2 spaces (for en passant purposes)
		if (playerMovingPawn2Spaces(from, to)) {
			((Pawn)from.getPiece()).setMoved2SpacesLastMove(true);
		}

		//the to space only ever has a king or rook of the same color as the player if the player is castling
		if (to.hasPieceOfColor(playerColor)) {
			if (to.hasPiece("king")) {
				castle(from, to);
			} else if (to.hasPiece("rook")) {
				castle(to, from);
			}
		} else { //if the player isn't castling
			movePieceWithoutAccountingForSpecialCases(from, to);
		}
	}

	private boolean playerMovingPawn2Spaces(Space from, Space to) {
		if (from.hasPiece("pawn")) {
			try {
				Space twoSpacesInFrontOfPawn = spaces[from.getLoc()[0]][from.getLoc()[1] + 2*((Pawn)from.getPiece()).getOneForward()];
				if (to == twoSpacesInFrontOfPawn) {
					return true;
				}
			} catch (IndexOutOfBoundsException e) { //if there is no space 2 spaces in front of the pawn
				return false;
			}
		}
		
		return false;
	}

	private boolean playerDoingEnPassant(Space from, Space to) {
		//if a pawn is moving diagonaly into an empty space, that means it's doing en passant
		return to.isEmpty() && 
		(pawnMovingDiagonalyInDirection("left", from, to) || pawnMovingDiagonalyInDirection("right", from, to));
	}

	private boolean pawnMovingDiagonalyInDirection(String direction, Space from, Space to) {
		if (from.hasPiece("pawn")) {
			try {
				Space diagonal;
				if (direction.equals("left")) {
					diagonal = spaces[from.getLoc()[0] - 1][from.getLoc()[1] + ((Pawn)from.getPiece()).getOneForward()];
				} else {
					diagonal = spaces[from.getLoc()[0] + 1][from.getLoc()[1] + ((Pawn)from.getPiece()).getOneForward()];
				}
				return to == diagonal;
			} catch (IndexOutOfBoundsException e) { //if there is no space diagonally in the specified direction
				//do nothing
			}
		}

		return false;
	}

	private void killEnPassantStyle(Space from, Space to) {
		Space spaceWithPawnToKill = spaces[to.getLoc()[0]][to.getLoc()[1] - ((Pawn)from.getPiece()).getOneForward()];
		spaceWithPawnToKill.setPiece(null);
		spaceWithPawnToKill.setPawnJustDiedFromEnPassantHere(true);
	}

	private void resetEnPassantVars() {
		//set moved2SpacesLastMove and pawnJustDiedFromEnPassantHere for all pieces in rows 3 and 4 
		//(because that's where you go after moving 2 spaces and the only place you can die from en passant)
		//to false, so that they're only true for the duration of 1 turn
		resetEnPassantVarsInRow(3);
		resetEnPassantVarsInRow(4);
	}

	//loop through every space in a row and reset it's en passant related variables
	private void resetEnPassantVarsInRow(int row) {
		for (Space[] column : spaces) {
			Space space = column[row];

			//keep a backup in case a turn is undone and the variables have to be restored
			space.setBackupPawnJustDiedFromEnPassantHere(space.getPawnJustDiedFromEnPassantHere());

			space.setPawnJustDiedFromEnPassantHere(false);

			if (space.hasPiece("pawn")) {
				((Pawn)space.getPiece()).setBackupMoved2SpacesLastMove(((Pawn)space.getPiece()).getMoved2SpacesLastMove());
				((Pawn)space.getPiece()).setMoved2SpacesLastMove(false);
			}
		}
	}

	private void restoreEnPassantVars() {
		restoreEnPassantVarsInRow(3);
		restoreEnPassantVarsInRow(4);
	}

	//loop through every space in a row and restore it's en passant related variables to their backups
	private void restoreEnPassantVarsInRow(int row) {
		for (Space[] column : spaces) {
			Space space = column[row];

			space.setPawnJustDiedFromEnPassantHere(space.getBackupPawnJustDiedFromEnPassantHere());
			if (space.hasPiece("pawn")) {
				((Pawn)space.getPiece()).setMoved2SpacesLastMove(((Pawn)space.getPiece()).getBackupMoved2SpacesLastMove());
			}
		}
	}

	private void unmovePiece(Space cameFrom, Space wentTo, char playerColor) {
		//if we're unmoving a pawn
		if (wentTo.hasPiece("pawn")) {
			//bring back the pawns to it's left and right that may have died from en passant
			revivePotentialEnPassantVictims(cameFrom, playerColor);

			//if it moved 2 spaces
			try {
				Space twoSpacesInFrontOfCameFrom = spaces[cameFrom.getLoc()[0]][cameFrom.getLoc()[1] + 2*((Pawn)wentTo.getPiece()).getOneForward()]; 
				if (twoSpacesInFrontOfCameFrom.equals(wentTo)) {
					((Pawn)wentTo.getPiece()).setMoved2SpacesLastMove(false);
				}
			} catch (IndexOutOfBoundsException e) { //if there is no space two spaces in front
				//do nothing
			}
		}

		//the wentTo is only ever empty if a castling was done,
		//because the piece doesn't actually move to the space the player clicked during castling
		if (wentTo.isEmpty()) {
			uncastle(cameFrom, wentTo);
		} else {
			unmovePieceWithoutAccountingForSpecialCases(cameFrom, wentTo);
		}
	}

	private void revivePotentialEnPassantVictims(Space murderousPawnCameFrom, char playerColor) {
		try {
			Space leftOfMurderousPawnCameFrom = spaces[murderousPawnCameFrom.getLoc()[0] - 1][murderousPawnCameFrom.getLoc()[1]];
			revivePotentialEnPassantVictim(leftOfMurderousPawnCameFrom, playerColor);   
		} catch (IndexOutOfBoundsException e) { //if there is no space to the left of the pawn
			//do nothing
		}

		try {
			Space rightOfMurderousPawnCameFrom = spaces[murderousPawnCameFrom.getLoc()[0] + 1][murderousPawnCameFrom.getLoc()[1]];
			revivePotentialEnPassantVictim(rightOfMurderousPawnCameFrom, playerColor);
		} catch (IndexOutOfBoundsException e) { //if there is no space to the right of the pawn
			//do nothing
		}
	}
	
	private void revivePotentialEnPassantVictim(Space space, char murderousPlayerColor) {
		if (space.getPawnJustDiedFromEnPassantHere()) {

			space.setPiece(new Pawn(oppositeColor(murderousPlayerColor), spaces));

			//any pawn that just died from en passant necessarily moved 2 spaces last move
			((Pawn)space.getPiece()).setMoved2SpacesLastMove(true);
			((Pawn)space.getPiece()).setBackupMoved2SpacesLastMove(true);
			((Pawn)space.getPiece()).hasMoved = true;

			space.setPawnJustDiedFromEnPassantHere(false);
			space.setBackupPawnJustDiedFromEnPassantHere(false);
		}
	}

	private void castle(Space rookFrom, Space kingFrom) {
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

	private void uncastle(Space cameFrom, Space wentTo) {
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
	
	private void movePieceWithoutAccountingForSpecialCases(Space from, Space to) {
		to.setPiece(from.getPiece());
		from.setPiece(null);
		//make sure we know that the piece has moved, for pawns moving 2 spaces and castling purposes
		to.getPiece().backupHasMoved = to.getPiece().hasMoved; //for move undoing purposes
		to.getPiece().hasMoved = true;
	}

	private void unmovePieceWithoutAccountingForSpecialCases(Space cameFrom, Space wentTo) {
		cameFrom.setPiece(wentTo.getPiece());
		wentTo.setPiece(null);
		//set hasMoved to what it was before the last move, for pawns moving 2 spaces and castling purposes
		cameFrom.getPiece().hasMoved = cameFrom.getPiece().backupHasMoved;
	}

	private boolean isInCheck(char playerColor) {
		ArrayList<int[]> dangerousLocs = new ArrayList<>();

		//loop through all the spaces and find all the dangerous locations
		for (Space[] column : spaces) {
			for (Space space : column) {
				//if the space contains an opponent's piece, all the spaces it can attack are dangerous
				if (space.hasPieceOfDifferentColor(playerColor)) {
					if (space.hasPiece("pawn")) {
						//If the space has a pawn, only consider diagonal attacking moves as dangerous
						dangerousLocs.addAll(((Pawn)space.getPiece()).diagonalsInFront(space.getLoc()));
					} else {
						dangerousLocs.addAll(space.legalMoves());
					}
				}
			}
		}
		//if the king is in a dangerous loc, return true
		int[] kingLoc = getKingLoc(playerColor);
		for (int[] dangerousLoc : dangerousLocs) {
			if (Arrays.equals(dangerousLoc, kingLoc))
			{
				return true;
			}
		}

		return false;
	}

	private int[] getKingLoc(char color) {
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

	private boolean gameEnd() {
		JLabel gameEndLabel;

		if (isInCheck('b') && allMovesLeadToCheck('b')) {
			gameEndLabel = new JLabel("Checkmate. White wins!", JLabel.CENTER);
		} else if (isInCheck('w') && allMovesLeadToCheck('w')) {
			gameEndLabel = new JLabel("Checkmate. Black wins!", JLabel.CENTER);
		} else if ((!isInCheck('b') && allMovesLeadToCheck('b')) ||
					(!isInCheck('w') && allMovesLeadToCheck('w'))) {
			gameEndLabel = new JLabel("It's a tie!", JLabel.CENTER);
		} else {
			return false;
		}

		gameEndLabel.setFont(new Font("TimesRoman", Font.PLAIN, (int)(chessBoard.getHeight()/20))); //20 is an arbitrary number
		chessBoard.getContentPane().add(BorderLayout.SOUTH, gameEndLabel);
		//wait because java may still be moving the pieces around in every way possible in the allMovesLeadToCheck function
		//so if we redraw the frame now, it might draw a piece at some random position before it gets unmoved
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			//this will never happen
		}
		chessBoard.pack(); //so that the frame is big enough to fit the gameEndLabel

		return true;
	}

	private boolean allMovesLeadToCheck(char playerColor) {
		//loop through every moveable piece
		ArrayList<int[]> moveablePieceLocs = getMoveablePieceLocs(playerColor);
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

				movePiece(from, to, playerColor);

				//if the move doesn't cause check, it means there's at least one move that will escape check
				if (!isInCheck(playerColor)) {
					//undo the move
					unmovePiece(from, to, playerColor);
					to.setPiece(tempPiece);

					chessBoard.repaint(); //sometimes because of the weird order java likes to do things,
					//the board is repainted after a piece is moved, but java takes too long to repaint
					//the board after unmoving the piece, so you can see the piece move by itself for a few
					//milliseconds. Forcing java to repaint here fixes that.
					return false;
				}

				//undo the move
				unmovePiece(from, to, playerColor);
				to.setPiece(tempPiece);
			}
		}

		chessBoard.repaint(); //(Same comment as above) sometimes because of the weird order java likes to do things,
		//the board is repainted after a piece is moved, but java takes too long to repaint
		//the board after unmoving the piece, so you can see the piece move by itself for a few
		//milliseconds. Forcing java to repaint here fixes that.
		return true;
	}

	private ArrayList<int[]> getMoveablePieceLocs(char playerColor) {
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