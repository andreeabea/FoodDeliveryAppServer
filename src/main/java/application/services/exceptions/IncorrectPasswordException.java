package application.services.exceptions;

public class IncorrectPasswordException extends Exception {

    public IncorrectPasswordException(String message)
    {
        super(message);
    }
}
