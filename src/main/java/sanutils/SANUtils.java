package sanutils;

import tscp.Board;
import tscp.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SANUtils {

    /**
     * Expression régulière permettant de valider une chaîne SAN.
     */
    // Découpage du pattern :
    // Mat/Pat/Nullité : (\\+{1,2}|#|\\(=\\))?
    // Petit roque : 0-0<Mat/Pat/Nullité>
    // Grand roque : 0-0-0<Mat/Pat/Nullité>
    // Pion sans prise : [a-h]([1-8]|[18][BKNQR])<Mat/Pat/Nullité>
    // Pion avec prise :
    // [a-h]x[a-h]((([1-8]|[18][BKNQR])<Mat/Pat/Nullité>)|([36]<Mat/Pat/Nullité> e\\.p\\.))
    // Pièces (sauf pion) : [BKNQR][a-h]?[1-8]?x?[a-h][1-8]<Mat/Pat/Nullité>
    public static final Pattern SAN_VALIDATOR
            = Pattern.compile("^(0-0(\\+{1,2}|#|\\(=\\))?)|(0-0-0(\\+{1,2}|#|\\(=\\))?)|"
                    + "([a-h]([1-8]|[18][BKNQR])(\\+{1,2}|#|\\(=\\))?)|"
                    + "([a-h]x[a-h]((([1-8]|[18][BKNQR])(\\+{1,2}|#|\\(=\\))?)|"
                    + "([36](\\+{1,2}|#|\\(=\\))? e\\.p\\.)))|"
                    + "([BKNQR][a-h]?[1-8]?x?[a-h][1-8](\\+{1,2}|#|\\(=\\))?)$");

    public static Move toMove(final Board gPosition, final String pSAN) throws SANException {
        if (gPosition == null) {
            throw new NullPointerException("Missing game state");
        }
        if (pSAN == null) {
            throw new NullPointerException("Missing SAN string");
        }

        if (!SAN_VALIDATOR.matcher(pSAN).matches()) {
            throw new SANException("Invalid SAN string [" + pSAN + ']', null);
        }

       final boolean trait = gPosition.getTrait() == BLANC;
        if ("0-0".equals(pSAN)) {
            // Gère les petits roques...
            if (trait) {
                return new GCoups(ROI, e1, g1, h1, f1, 0, Roque);

            }
            return new GCoups(ROI, e8, g8, h8, f8, 0, Roque);
           // return new GCoups(ROI, e1, c1, a1, d1, 0, ICodage.TYPE_DE_COUPS.Roque);
        } else if ("0-0-0".equals(pSAN)) {
            // ... les grands roques...
            if (trait) {
                return new GCoups(ROI, e1, c1, a1, d1, 0, Roque);
            }
            return new GCoups(ROI, e8, c8, a8, d8, 0, Roque);
            //return new GCoups(ROI, e8, g8, h8, f8, 0, ICodage.TYPE_DE_COUPS.Roque);
        }

        // Gère les coups normaux...
        final int piece;
        int posSrc = 0;
        char c = pSAN.charAt(posSrc);
        if (Character.isLowerCase(c)) {
            if (trait) {
                piece = BLANC * PION;
            } else {
                piece = NOIR * PION;
            }
        } else {
            if (trait) {
                piece = valueOf(c);
            } else {
                piece = valueOf(Character.toLowerCase(c));
            }
            posSrc++;
        }

        final boolean prise = pSAN.indexOf('x') >= 0;
//        final List<GCoups> mvts = new ArrayList<>(gPosition.getCoupsValides());
        final List<Move> mvts = gPosition.getCoupsValides();
//         System.out.println("gPosition83= " + gPosition.print());
//        System.out.println("mvts83= " + mvts);
        for (int i = mvts.size() - 1; i >= 0; i--) {
            final Move m = mvts.get(i);
            final boolean capture = m.getPiecePrise() != 0;
            if ((piece != m.getPiece()) || (prise != capture)) {
                mvts.remove(i);
            }
        }

        int posDst = pSAN.length() - 1;
        while ((posDst > 0) && (!Character.isDigit(pSAN.charAt(posDst)))) {
            posDst--;
        }
        final int dst = valueOf(pSAN.substring(posDst - 1, posDst + 1));
        for (int i = mvts.size() - 1; i >= 0; i--) {
            final Move m = mvts.get(i);
            if (dst != m.getCaseX()) {
                mvts.remove(i);
            }
        }
        if (mvts.size() == 1) {
            return mvts.get(0);
        }

        if ((mvts.size() > 1) && (((piece == NOIR * PION) && (getRank(dst) == 0))
                || ((piece == BLANC * PION) && (getRank(dst) == 7)))) {
            // Supprime les ambiguités dues aux promotions...
            posDst = pSAN.length() - 1;
            while ((posDst > 0) && ("BNQR".indexOf(pSAN.charAt(posDst)) < 0)) {
                posDst--;
            }
            if (posDst > 0) {
                c = pSAN.charAt(posDst);
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final int prom = mvts.get(i).getPiecePromotion();
                    int t = Math.abs(prom);
                    if ((prom == 0)
                            //                            || (prom.getType().getSANLetter().charAt(0) != c)) {
                            || (getSANLetter(t).charAt(0) != c)) {
                        mvts.remove(i);
                    }
                }
            } else {
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    if (mvts.get(i).getPiecePromotion() != 0) {
                        mvts.remove(i);
                    }
                }
            }
        }

        // Supprime les ambiguités...
        if (mvts.size() > 1) {
            c = pSAN.charAt(posSrc);
            if (Character.isLowerCase(c)) {
                final int col = c - 'a';
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final Move m = mvts.get(i);
                    if (col != getFile(m.getCaseO())) {
                        mvts.remove(i);
                    }
                }
                posSrc++;
            }
        }
        if (mvts.size() > 1) {
            c = pSAN.charAt(posSrc);
            if (Character.isDigit(c)) {
                final int lig = c - '1';
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final Move m = mvts.get(i);
                    if (lig != getRank(m.getCaseO())) {
                        mvts.remove(i);
                    }
                }
                posSrc++;
            }
        }

        final int l = mvts.size();
        if (l > 1) {
            throw new SANException("Ambiguous SAN string [" + pSAN + ']', null);
        } else if (l < 1) {
            throw new SANException("Illegal SAN string context [" + pSAN + ']', null);
        }

        return mvts.get(0);
    }

    private static int valueOf(final String pChaine) {
        if (pChaine == null) {
            throw new NullPointerException("Missing square string");
        }
        if (pChaine.length() != 2) {
            throw new IllegalArgumentException("Illegal square string [" + pChaine + ']');
        }

        return valueOf(pChaine.charAt(0) - 'a', pChaine.charAt(1) - '1');
    }

    private static int valueOf(final int pColonne, final int pLigne) {
        if ((pColonne < 0) || (pColonne >= 8)) {
            throw new IllegalArgumentException("Illegal file [" + pColonne + ']');
        }
        if ((pLigne < 0) || (pLigne >= 8)) {
            throw new IllegalArgumentException("Illegal rank [" + pLigne + ']');
        }

        return CASES117[pColonne + pLigne * 8];
    }

    private static int valueOf(char c) {
        int color = Character.isLowerCase(c) ? NOIR : BLANC;
        char t = Character.toLowerCase(c);
        switch (t) {
            case 'k':
                return color * ROI;
            case 'q':
                return color * DAME;
            case 'b':
                return color * FOU;
            case 'n':
                return color * CAVALIER;
            case 'r':
                return color * TOUR;
            default:
                return 0; //erreur
        }
    }

    /**
     * Renvoi la chaine SAN correspondant à un mouvement pour un état
     * d'échiquier.
     *
     * @param gPosition
     * @param pMouvement
     * @return
     */
    public static String toSAN(Board gPosition, final Move pMouvement) {
        if (gPosition == null) {
            throw new NullPointerException("Missing game state");
        }
        if (pMouvement == null) {
            throw new NullPointerException("Missing move");
        }

        final boolean trait = gPosition.getTrait() == BLANC;
        final int piece = pMouvement.getPiece();
        final int t = Math.abs(piece);
        final StringBuilder sb = new StringBuilder();
        final int src = pMouvement.getCaseO();
        final int dst = pMouvement.getCaseX();

        UndoGCoups ug = new UndoGCoups();
        gPosition.exec(pMouvement, ug);
        final int nbMvts = gPosition.getCoupsValides(-gPosition.getTrait()).size();
        gPosition.unexec(ug);

        final int xSrc = getFile(src);
        final int xDst = getFile(dst);
        if ((t == ROI) && (Math.abs(xSrc - xDst) > 1)) {
            // Roques...
            sb.append("0-0");
            if (xSrc > xDst) {
                sb.append("-0");
            }
        } else {
            // Normal...
            sb.append(getSANLetter(t));

            // Recherche et levée des éventuelles ambiguités...
            if (t != PION) {
                final List<Move> mvts = new ArrayList<>(gPosition.getCoupsValides(gPosition.getTrait()));
//                System.out.println(mvts);
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final Move m = mvts.get(i);
                    if ((piece != m.getPiece()) || (dst != m.getCaseX()) || (m.equals(pMouvement))) {
                        mvts.remove(i);
                    }
                }
                boolean preciser = true;
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final GCoups m = mvts.get(i);
                    if (xSrc != getFile(m.getCaseO())) {
                        mvts.remove(i);
                        if (preciser) {
                            sb.append((char) ('a' + xSrc));
                            preciser = false;
                        }
                    }
                }
                final int ySrc = getRank(src);
                for (int i = mvts.size() - 1; i >= 0; i--) {
                    final Move m = mvts.get(i);
                    if (ySrc != getRank(m.getCaseO())) {
                        sb.append((char) ('1' + ySrc));
                        break;
                    }
                }
            }

            if ((gPosition.getEtats()[dst] != 0) || ((dst == gPosition.getCaseEP()) && (t == PION))) {
                // Prise...
                if (t == PION) {
                    sb.append((char) ('a' + xSrc));
                }
                sb.append('x');
            }

            sb.append(getFENString(dst));

            if (t == PION) {
                // Cas particuliers...
                if (dst == gPosition.getCaseEP()) {
                    // ... de la prise en passant...
                    sb.append(" e.p.");
                } else {
                    // ... ou de la promotion...
                    final int yDst = getRank(dst);
                    if ((trait && (yDst == 8 - 1))
                            || ((!trait) && (yDst == 0))) {
                        // Le '=' n'est pas dans la version de SAN de la FIDE :
                        // sb.append('=');
                        final int promotion = pMouvement.getPiecePromotion();
                        if (promotion == 0) {
                            assert false;
                            sb.append(getSANLetter(DAME));
                        } else {
                            final int promotionType = Math.abs(promotion);
                            if (promotionType != PION) {
                                sb.append(getSANLetter(promotionType));
                            } else {
                                assert false;
                            }
                        }
                    }
                }
            }
        }

        if (gPosition.isInCheck(-gPosition.getTrait())) {
            // Echec / Mat ...
            sb.append('+');
            if (nbMvts == 0) {
                sb.append('+');
            }
        } else if (nbMvts == 0) {
            // Pat ...
            sb.append("(=)");
        }
        final String res = sb.toString();
        assert SAN_VALIDATOR.matcher(res).matches();

        return res;
    }

    private static String getSANLetter(int t) {
        switch (t) {
            case ROI:
                return "K";
            case DAME:
                return "Q";
            case FOU:
                return "B";
            case CAVALIER:
                return "N";
            case TOUR:
                return "R";
            default:
                return ""; //erreur
        }
    }

    private static String getFENString(int dst) {
        final StringBuilder sb = new StringBuilder();
        sb.append((char) ('a' + getFile(dst))).append((char) ('1' + getRank(dst)));
        return sb.toString();
    }

    public static int getFile(int _case) {
        return (_case - 12 * ((int) _case / 12)) - 2;
    }

    public static int getRank(int _case) {
        int r = 7 - (9 - (int) (_case / 12));
        return r;
    }

}
