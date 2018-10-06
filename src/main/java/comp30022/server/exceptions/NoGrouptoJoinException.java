package comp30022.server.exceptions;

public class NoGrouptoJoinException extends Exception{
    public NoGrouptoJoinException(){
        super("There is No Group to join");
    }
}
