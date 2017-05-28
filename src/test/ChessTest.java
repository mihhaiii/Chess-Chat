package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import chess.ChessWindow.Piece;
import chess.ChessWindow;
import chess.ChessWindow.Bishop;
import chess.ChessWindow.King;
import chess.ChessWindow.Knight;
import chess.ChessWindow.Pawn;
import chess.ChessWindow.Queen;
import chess.ChessWindow.Rook;;

public class ChessTest {
	@Test
	public void BishopMoveValidFalseWrongDirectionTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[0][2].getPiece().isMoveLegal(1, 2);
		assertFalse(isValid);
	}
	
	@Test
	public void KnightMoveValidTrueTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[0][1].getPiece().isMoveLegal(2, 0);
		assertTrue(isValid);
	}
	
	@Test
	public void KnightMoveValidFalseWrongDirectionTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[0][1].getPiece().isMoveLegal(2, 1);
		assertFalse(isValid);
	}
	
	@Test
	public void KnightMoveValidFalseSpotTakenTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[0][1].getPiece().isMoveLegal(3, 1);
		assertFalse(isValid);
	}
	
	@Test
	public void QueenMoveValidFalsePiecesBeforeTargetTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[0][3].getPiece().isMoveLegal(4, 3);
		assertFalse(isValid);
	}
	
	@Test
	public void PawnMoveValidTrueTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[1][0].getPiece().isMoveLegal(2, 0);
		assertTrue(isValid);
	}
	
	@Test
	public void PawnMoveValidFalseMoreThanTwoSquaresTest() throws Exception {
		ChessWindow chessWindow = null;
		chessWindow = new ChessWindow();
		boolean isValid = chessWindow.board[1][0].getPiece().isMoveLegal(4, 0);
		assertFalse(isValid);
	}
}