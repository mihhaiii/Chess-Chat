package chess;

import chess.ChessWindow.Cell;
import chess.ChessWindow.Piece;
import chess.ChessWindow.Rook;
import chess.ChessWindow.Bishop;

public interface PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved);
}

class PawnMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		if (color.equals("white")) {
			if (x == 0)
				return false;
			if (x == 6 && xx == 4 && y == yy) {
				if (board[xx][yy].getPiece() == null && board[x - 1][yy].getPiece() == null)
					return true;
			}
			if (y == yy && x - 1 == xx && board[xx][yy].getPiece() == null) {
				return true;
			}
			Piece ad;
			if (x - 1 == xx && (y - 1 == yy || y + 1 == yy) && (ad = board[xx][yy].getPiece()) != null
					&& ad.getColor() == "black") {
				return true;
			}
			return false;
		} else {
			if (x == 7)
				return false;
			if (x == 1 && xx == 3 && y == yy) {
				if (board[xx][yy].getPiece() == null && board[x + 1][yy].getPiece() == null)
					return true;
			}
			if (y == yy && x + 1 == xx && board[xx][yy].getPiece() == null) {
				return true;
			}
			Piece ad;
			if (x + 1 == xx && (y - 1 == yy || y + 1 == yy) && (ad = board[xx][yy].getPiece()) != null
					&& ad.getColor() == "white") {
				return true;
			}
			return false;
		}
	}
}

class RookMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		if (x == xx) {
			for (int y1 = Math.min(y, yy) + 1; y1 < Math.max(y, yy); y1++) {
				if (board[x][y1].getPiece() != null)
					return false;
			}
			if (board[xx][yy].getPiece() != null && board[xx][yy].getPiece().getColor().equals(color))
				return false;
			return true;
		}
		if (y == yy) {
			for (int x1 = Math.min(x, xx) + 1; x1 < Math.max(x, xx); x1++) {
				if (board[x1][y].getPiece() != null)
					return false;
			}
			if (board[xx][yy].getPiece() != null && board[xx][yy].getPiece().getColor().equals(color))
				return false;
			return true;
		}
		return false;
	}
}

class BishopMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		Piece p = board[xx][yy].getPiece();
		if (p != null && p.getColor().equals(color))
			return false;
		if (Math.abs(x - xx) != Math.abs(y - yy))
			return false;
		int dx = (x > xx ? -1 : 1);
		int dy = (y > yy ? -1 : 1);
		x += dx;
		y += dy;
		while (!(x == xx && y == yy)) {
			if (board[x][y].getPiece() != null)
				return false;
			x += dx;
			y += dy;
		}
		return true;
	}
}

class KingMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		if (!pieceMoved && x == xx/* same line */) {
			// try casling
			Piece rock1 = board[x][0].getPiece();
			Piece rock2 = board[x][7].getPiece();
			if (!rock1.hasPieceMoved()) {
				if (Math.abs(y - yy) == 2 && yy < y) {
					boolean castlePossible1 = true;
					for (int y1 = 0; y1 <= y; y1++)
						if (true/*
								 * board[x][y1].isUnderAttackBy(getRevColor( ))
								 */)
							castlePossible1 = false;
					if (castlePossible1) {
						// rock1.moveTo(board[x][yy+1]);
						return true;
					}
				}
			}
			if (!rock2.hasPieceMoved()) {
				if (Math.abs(y - yy) == 2 && y < yy) {
					boolean castlePossible1 = true;
					for (int y1 = y; y1 <= yy; y1++)
						if (true/*
								 * board[x][y1].isUnderAttackBy(getRevColor( ))
								 */)
							castlePossible1 = false;
					if (castlePossible1) {
						// rock2.moveTo(board[x][yy-1]);
						return true;
					}
				}
			}
		}
		if (Math.abs(x - xx) <= 1 && Math.abs(y - yy) <= 1) {
			if (!(board[xx][yy].getPiece() != null && board[xx][yy].getPiece().getColor().equals(color)))
				return true;
		}
		return false;
	}
}

class KnightMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		Piece p = board[xx][yy].getPiece();
		if (p != null && p.getColor().equals(color)) {
			return false;
		}
		int difx = Math.abs(x - xx);
		int dify = Math.abs(y - yy);
		if ((difx == 1 && dify == 2) || (difx == 2 && dify == 1))
			return true;
		return false;
	}
}



class QueenMove implements PieceMove {
	public boolean isValidMove(int x, int y, int xx, int yy, String color, Cell board[][], boolean pieceMoved) {
		PieceMove pieceMoveRook = new RookMove();
		PieceMove pieceMoveBishop = new BishopMove();

		boolean rookValid = pieceMoveRook.isValidMove(x, y, xx, yy, color, board, pieceMoved);
		boolean bishopValid = pieceMoveBishop.isValidMove(x, y, xx, yy, color, board, pieceMoved);
		boolean answer = (rookValid || bishopValid);
		return answer;
	}
}


