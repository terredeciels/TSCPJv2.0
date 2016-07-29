package ggame;

import tools.FenToBoard;
import tscp.Board;
import tscp.Move;

/*
    @TODO Thread
 */
public class GGame {
    String FEN_INITIALE = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public Move lastmove;
    public Board gPosition;

    public GGame() {
        gPosition = FenToBoard.toBoard(FEN_INITIALE);
    }

    public void setGPositionMove(Board gPosition) {
        this.gPosition = gPosition;
    }

    public Board getGPositionMove() {
        return gPosition;
    }

    public void resetTo() {
        gPosition = FenToBoard.toBoard(FEN_INITIALE);
    }
}
