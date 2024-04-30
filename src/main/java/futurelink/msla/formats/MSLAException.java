package futurelink.msla.formats;

public class MSLAException extends Exception {
    public MSLAException(String message) {
        super(message);
    }

    public MSLAException(String message, Throwable cause) {
        super(message, cause);
    }
}
