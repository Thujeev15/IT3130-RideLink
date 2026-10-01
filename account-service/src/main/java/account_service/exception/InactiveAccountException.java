package account_service.exception;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException() { super("This account is not active"); }
}
