package comp30022.server.exception;

public class DbException extends RuntimeException {
    public DbException(String message){
        super(message);
    }
}
