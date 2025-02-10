package uk.gov.justice.services.event.sourcing.subscription.error;

public class ExceptionHashingException extends RuntimeException {

    public ExceptionHashingException(final String message) {
        super(message);
    }

    public ExceptionHashingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
