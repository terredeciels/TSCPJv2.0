package ia;

import ia.Eval;
import tscp.Board;
import tscp.Move;

import java.util.List;


public class AlphaBeta {

    int MAT_VALUE = Integer.MIN_VALUE / 2;
    private final int depth;
    private final Board gp;
    private final Eval f_eval;

    public AlphaBeta(Board gp, Eval f_eval, int depth) {
        this.f_eval = f_eval;
        this.gp = gp;
        this.depth = depth;
    }

    private int alphabeta(final Board gp, final int pProfondeur, final int pAlpha, final int pBeta) {

        final int trait = gp.getTrait();
        if (pProfondeur == 0) {
            return evaluate(gp, trait);
        }

        final List<Move> coups = getValidMoves(gp, trait);

        final int l = coups.size();
        if (l == 0) {
            return evaluate(gp, trait);
        }

        int res = MAT_VALUE - 1;
        int alpha = pAlpha;

        for (final Move mvt : coups) {
            UndoGCoups ug = new UndoGCoups();
            gp.exec(mvt, ug);
            final int note = -alphabeta(gp, pProfondeur - 1, -pBeta, -alpha);
            gp.unexec(ug);

            if (note > res) {
                res = note;
                if (res > alpha) {
                    alpha = res;
                    if (alpha > pBeta) {
                        return res;
                    }
                }
            }
        }

        return res;
    }


    public Move search() {
        return searchMoveFor(gp, gp.getCoupsValides());
    }

    public Move searchMoveFor(final Board gp, final List<Move> pCoups) {

        final int l = pCoups.size();

        Move res = pCoups.get(0);
        int alpha = MAT_VALUE - 1;
        for (final Move mvt : pCoups) {

            UndoGCoups ug = new UndoGCoups();
            gp.exec(mvt, ug);
            final int note = -alphabeta(gp, depth - 1, MAT_VALUE, -alpha);
            gp.unexec(ug);
            if ((note > alpha)) {
                alpha = note;
                res = mvt;
            }
        }
        return res;
    }

    private int evaluate(Board gp, int trait) {
        return f_eval.evaluate(gp, trait);
    }

    private List<Move> getValidMoves(Board gp, int trait) {
        return gp.getCoupsValides(trait);
    }

}
