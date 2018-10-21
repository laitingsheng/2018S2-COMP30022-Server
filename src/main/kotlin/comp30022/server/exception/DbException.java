package comp30022.server.exception;

/**
 * Exception raised when there is error with firebase
 */
public class DbException extends RuntimeException {
    public DbException(String message) {
        super(message);
    }
}
