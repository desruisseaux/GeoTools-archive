package it.geosolutions.utils.progress;

/**
 * Event launched when an exception occurrs. Percentage and message may be missing, in this case
 * they will be -1 and the exception message (localized if available, standard otherwise)
 * 
 * @author aaime
 * 
 */
public class ExceptionEvent extends ProcessingEvent {

    private Exception exception;

    public ExceptionEvent(Object source, String message, double percentage, Exception exception) {
        super(source, message, percentage);
        this.exception = exception;
    }

    public ExceptionEvent(Object source, Exception exception) {
        super(source, getMessageFromException(exception), -1);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
    
    static String getMessageFromException(Exception exception) {
        if(exception.getLocalizedMessage() != null)
            return exception.getLocalizedMessage();
        else
            return exception.getMessage();
    }

}
