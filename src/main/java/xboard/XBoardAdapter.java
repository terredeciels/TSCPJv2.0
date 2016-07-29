package xboard;

import ggame.GGame;
import ia.Search;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import sanutils.SANException;
import tools.FenToBoard;
import tscp.Board;
import tscp.Move;

import static sanutils.SANUtils.toMove;
import static sanutils.SANUtils.toSAN;

/**
 * interface entre l'I.A. et une I.H.M. ('Arena' par ex.) utilisant le protocole
 * XBoard/WinBoard.
 */
public final class XBoardAdapter {

    public static final String APPLICATION_NAME = "GCLE";
    public static final String APPLICATION_VERSION = "1.0";
    private static final String APPLICATION_STRING = APPLICATION_NAME + " " + APPLICATION_VERSION;
    private static final String FEATURES_STRING
            = "feature analyze=0 colors=0 myname=\"" + APPLICATION_STRING
            + "\" pause=0 ping=1 playother=0 san=1 setboard=1 sigint=0 sigterm=0 "
            + "time=0 usermove=1 variants=\"normal\" done=1";
    private static final Logger LOGGER = Logger.getLogger(XBoardAdapter.class.getName());
    private static final GGame game = new GGame();
    private static Board gPosition;
    private static boolean S_forceMode = false;
    /**
     * Etat du mode de jeu faible/fort.
     */
    private static boolean S_hardMode;// a voir
    private static boolean S_illegalPosition;
    public static boolean DEBUG = false;
    private static String xbSAN;

    private XBoardAdapter() {
    }

    public static void main(final String[] pArgs) throws SANException {
        assert pArgs != null;

        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter command (debug):");

        while (true) {
            String commande = null;
            try {
                commande = in.readLine();
            } catch (final IOException e) {
                LOGGER.severe(e.toString());
            }
            if (commande == null) {
                LOGGER.severe("Communication error.");
                System.exit(-1);
            } else {
                parseCommand(commande.trim());
            }
        }
    }

    public static void parseCommand(final String pCommande) throws SANException {
        assert pCommande != null;
        gPosition = game.gPosition;

        if (pCommande.startsWith("accepted ")) {
            // Tant mieux, mais il n'y a rien à faire.
        } else if (pCommande.startsWith("level ")) {
            // Non impléméntée...
        } else if (pCommande.startsWith("ping ")) {
            System.out.println("pong" + pCommande.substring(4));
        } else if (pCommande.startsWith("protover ")) {
            System.out.println(FEATURES_STRING);
        } else if (pCommande.startsWith("rejected ")) {
            System.out.println("tellusererror Missing feature " + pCommande.substring(9));
        } else if (pCommande.startsWith("result ")) {
            // Non impléméntée...
        } else if (pCommande.startsWith("setboard ")) {
            final String fen = pCommande.substring(9);
            gPosition = FenToBoard.toBoard(fen);
            game.setGPositionMove(gPosition);
            if (gPosition == null) {
                S_illegalPosition = true;
                System.out.println("tellusererror Illegal position");
            } else {
                S_illegalPosition = false;
            }
        } else if (pCommande.startsWith("usermove ")) {

            xbSAN = pCommande.substring(9);
            String san = xbSAN.replace("=", "").replace('O', '0');

            Move mvt = toMove(gPosition, san);
            if ((mvt == null) || S_illegalPosition) {
                System.out.println("Illegal move: " + xbSAN);
            } else {

                UndoGCoups ug = new UndoGCoups();
                gPosition.exec(mvt, ug);

                gPosition._fullmoveNumber++;
                game.lastmove = mvt;

                if (!S_forceMode) {
                    think();
                }
            }
        } else if (pCommande.equals("computer")) {
            // Non impléméntée...
        } else if (pCommande.equals("easy")) {
            S_hardMode = false;
        } else if (pCommande.equals("force")) {
            S_forceMode = true;
        } else if (pCommande.equals("go")) {
            if (S_illegalPosition) {
                System.out.println("Error (illegal position): go");
            } else {
                S_forceMode = false;
                think();
            }
        } else if (pCommande.equals("hard")) {
            S_hardMode = true;
        } else if (pCommande.equals("new")) {
            game.resetTo();
            S_forceMode = false;
            S_illegalPosition = false;
        } else if (pCommande.equals("nopost")) {
            // Non impléméntée...
        } else if (pCommande.equals("post")) {
            // Non impléméntée...
        } else if (pCommande.equals("quit")) {
            System.exit(0);
        } else if (pCommande.equals("random")) {
            // Rien à faire.
        } else if (pCommande.equals("xboard")) {
            System.out.println(APPLICATION_STRING + " started in xboard mode.");
        } else {
            System.out.println("Error (unknown command): " + pCommande);
        }
    }

    private static void think() {

        Search search = new Search(gPosition);
        Move gcoups_curr = search.getBestMove();

        String san = toSAN(gPosition, gcoups_curr);
        UndoGCoups ug = new UndoGCoups();
        gPosition.exec(gcoups_curr, ug);

        game.setGPositionMove(gPosition);
        gPosition._fullmoveNumber++;
        game.lastmove = gcoups_curr;

        xbSAN = san.replace(" e.p.", "").replace("O", "0");
        System.out.println("move " + xbSAN);
    }

    public static String getXbSAN() {
        return xbSAN;
    }

}
