package uk.gov.justice.services.subscription;

public class ProcessedEventTrackingException extends RuntimeException {

    public ProcessedEventTrackingException(final String message) {
        super(message);
    }

    public ProcessedEventTrackingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
