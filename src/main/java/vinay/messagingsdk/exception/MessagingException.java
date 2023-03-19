package vinay.messagingsdk.exception;

public class MessagingException extends Exception {

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable t) {
        super(message, t);
    }
}
