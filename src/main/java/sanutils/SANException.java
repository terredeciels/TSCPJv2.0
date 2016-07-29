package sanutils;

/**
 * Signale une erreur dans une chaîne SAN. Les instances de cette classe sont
 * des immuables : classe sûre vis-à-vis des threads.
 */
public final class SANException extends Exception {

    /**
     * Identifiant de la classe pour la sérialisation.
     */
    private static final long serialVersionUID = -2041023759257130798L;

    /**
     * Instancie une nouvelle exception.
     */
    SANException(final String pMessage, final Throwable pErreur) {
        super(pMessage, pErreur);
    }
}
