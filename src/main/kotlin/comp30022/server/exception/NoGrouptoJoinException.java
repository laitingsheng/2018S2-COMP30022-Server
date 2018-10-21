package comp30022.server.exception;

/**
 * Exception when there is no group that user will join
 * The server is expect to createGroup for user after receive this exception
 */
public class NoGrouptoJoinException extends Exception {
    public NoGrouptoJoinException() {
        super("There is No Group to join");
    }
}
