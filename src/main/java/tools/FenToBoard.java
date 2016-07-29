package tools;

import org.chesspresso.Chess;
import org.chesspresso.position.Position;
import tscp.Board;
import tscp.Constants;

public class FenToBoard implements Constants {

    public static Board toBoard(String fen) {
        return toBoard(new Position(fen));
    }

    private static Board toBoard(Position p) {
        Board board = new Board();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int casecp = 56 - 8 * i + j;
                board.piece[j + 8 * i]
                        = abs(p.getStone(casecp)) == 0 ? 6
                        : abs(p.getStone(casecp)) == 6 ? 5
                        : abs(p.getStone(casecp)) == 5 ? 0 : abs(p.getStone(casecp));
                board.color[j + 8 * i]
                        = p.getStone(casecp) < 0 ? LIGHT : p.getStone(casecp) > 0 ? DARK : EMPTY;
            }
        }
        board.side = p.getToPlay() == Chess.WHITE ? LIGHT : DARK;
        board.xside = p.getToPlay() == Chess.WHITE ? DARK : LIGHT;

        int cp_roques = p.getCastles();
        board.castle = (cp_roques & 1) == 1 ? 2 : 0;
        board.castle += (cp_roques & 2) == 2 ? 1 : 0;
        board.castle += (cp_roques & 4) == 4 ? 8 : 0;
        board.castle += (cp_roques & 8) == 8 ? 4 : 0;

        board.ep = p.getSqiEP() == -1 ? -1 : p.getSqiEP();
        return board;
    }

    private static int abs(int x) {
        return x < 0 ? -x : x;
    }

}
