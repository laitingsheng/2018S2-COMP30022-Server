package comp30022.server.exception;

public class NoGrouptoJoinException extends Exception {
    public NoGrouptoJoinException() {
        super("There is No Group to join");
    }
}
