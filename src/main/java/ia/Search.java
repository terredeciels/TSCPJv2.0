package ia;


import tools.FenToBoard;
import tscp.Board;
import tscp.Move;

/**
 * @TODO Runnable
 */
public class Search {

    private final Board gPosition;
    private final int depth;
    private Eval f_eval;
    private AlphaBeta ia;

    public Search(Board gPosition) {
        this.gPosition = gPosition;
        depth = 4;
        f_eval = new Eval();
        ia = new AlphaBeta(gPosition, f_eval, depth);
    }

    public Search(String f) {
        gPosition = FenToBoard.toBoard(f);
        depth = 4;
        f_eval = new Eval();
        ia = new AlphaBeta(gPosition, f_eval, depth);
    }

    public final Move getBestMove() {
        return ia.search();
    }

    public Board getGPositionMove() {
        return gPosition;
    }

}
