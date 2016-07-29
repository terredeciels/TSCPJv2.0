package ia;

import tscp.Board;

import static sanutils.SANUtils.getFile;
import static sanutils.SANUtils.getRank;

/**
 * Fonction d'évaluation : matériel et la position des pièces
 */
public class Eval {

    int MAT_VALUE = Integer.MIN_VALUE / 2;
    /**
     * Bonus/Malus d'un cavalier (blanc par défaut) en fonction de sa position.
     */
    private static final int[] KNIGHT_POSITIONS = { //  
        -50, -30, -30, -30, -30, -30, -30, -50, // a1 ... h1
        -30, -20, -20, -20, -20, -20, -20, -30, // a2 ... h2
        -20, 0, 20, 20, 20, 20, 0, -20, // a3 ... h3
        -20, 0, 20, 20, 20, 20, 0, -20, // a4 ... h4
        -20, 0, 10, 20, 20, 10, 0, -20, // a5 ... h5
        -20, 0, 10, 10, 10, 10, 0, -20, // a6 ... h6
        -20, -10, 0, 0, 0, 0, -10, -20, // a7 ... h7
        -40, -20, -20, -20, -20, -20, -20, -40, // a8 ... h8
    };

    static {
        assert KNIGHT_POSITIONS.length == 64;
    }

    /**
     * Bonus/Malus d'un pion (blanc par défaut) en fonction de sa position.
     */
    private static final int[] PAWN_POSITIONS = {
        0, 0, 0, 0, 0, 0, 0, 0, // a1 ... h1
        2, 2, 2, -2, -2, 2, 2, 2, // a2 ... h2
        -2, -2, -2, 4, 4, -2, -2, -2, // a3 ... h3
        0, 0, 0, 4, 4, 0, 0, 0, // a4 ... h4
        2, 4, 6, 8, 8, 6, 4, 2, // a5 ... h5
        4, 6, 8, 10, 10, 8, 6, 4, // a6 ... h6
        4, 6, 8, 10, 10, 8, 6, 4, // a7 ... h7
        500, 500, 500, 500, 500, 500, 500, 500, // a8 ... h8
    };

    static {
        assert PAWN_POSITIONS.length == 64;
    }

    /**
     * Bonus/Malus de base liés à la position d'une pièce (symétrique : adapté
     * aux deux couleurs).
     */
    private static final int[] DEFAULT_POSITIONS = {
        0, 0, 0, 0, 0, 0, 0, 0, // a1 ... h1
        0, 0, 0, 5, 5, 0, 0, 0, // a2 ... h2
        0, 0, 5, 5, 5, 5, 0, 0, // a3 ... h3
        0, 5, 5, 10, 10, 5, 5, 0, // a4 ... h4
        0, 5, 5, 10, 10, 5, 5, 0, // a5 ... h5
        0, 0, 5, 5, 5, 5, 0, 0, // a6 ... h6
        0, 0, 0, 5, 5, 0, 0, 0, // a7 ... h7
        0, 0, 0, 0, 0, 0, 0, 0, // a8 ... h8
    };

    static {
        assert DEFAULT_POSITIONS.length == 64;
    }

    public Eval() {
    }

    public int evaluate(final Board gp, final int trait) {
        boolean pTrait = trait == BLANC;

        int res = -gp.getHalfmoveCount();

        for (int _case : CASES117) {
            int piece = gp.getEtats()[_case];
            if (piece != VIDE) {
                boolean traitPiece = isWhite(piece);
                int typePiece = typeDePiece(piece);
                int val = valueType(typePiece);
                int pos;
                switch (typePiece) {
                    case FOU:
                    case DAME:
                    case TOUR:
                        pos = DEFAULT_POSITIONS[getIndex(_case)];
                        break;
                    case ROI:
                        int _trait = traitPiece ? BLANC : NOIR;

                        if ((traitPiece != pTrait) && (gp.getFullmoveNumber() > 10) && gp.isInCheck(_trait)) {
                            if (gp.getCoupsValides(_trait).isEmpty()) {
                                // Malus pour un mat...
                                pos = MAT_VALUE;
                            } else {
                                // Malus pour un échec...
                                pos = -250;
                            }
                        } else {
                            // Pas de valeur de position pour le roi.
                            pos = 0;
                        }
                        break;
                    case CAVALIER:
                        if (traitPiece) {
                            pos = KNIGHT_POSITIONS[getIndex(_case)];
                        } else {
                            pos = KNIGHT_POSITIONS[((8 - 1) - getRank(_case)) * 8 + getFile(_case)];
                        }
                        break;
                    case PION:
                        if (traitPiece) {
                            pos = PAWN_POSITIONS[getIndex(_case)];
                        } else {
                            pos = PAWN_POSITIONS[((8 - 1) - getRank(_case)) * 8 + getFile(_case)];
                        }
                        break;
                    default:
                        assert false;
                        pos = 0;
                }
                int score = val + pos;
                if (traitPiece == pTrait) {
                    res += score;
                } else {
                    res -= score;
                }
            }
        }
        return res;
    }

    private boolean isWhite(int piece) {
        return piece < 0;
    }

    private int typeDePiece(int piece) {
        return (piece < 0) ? -piece : piece;
    }

    private int valueType(int piece) {
        int type_de_piece = (piece < 0) ? -piece : piece;
        return PieceType.getValue(type_de_piece);

    }

    private int getIndex(int _case) {
        return INDICECASES[_case];
    }
}
