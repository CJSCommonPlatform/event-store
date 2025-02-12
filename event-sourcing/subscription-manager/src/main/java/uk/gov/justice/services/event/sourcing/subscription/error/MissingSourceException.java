package uk.gov.justice.services.event.sourcing.subscription.error;

public class MissingSourceException extends RuntimeException {

    public MissingSourceException(final String message) {
        super(message);
    }
}
