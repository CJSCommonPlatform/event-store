package uk.gov.justice.services.event.sourcing.subscription.error;

public class MissingPoisitionInStreamException extends RuntimeException {

    public MissingPoisitionInStreamException(final String message) {
        super(message);
    }
}
