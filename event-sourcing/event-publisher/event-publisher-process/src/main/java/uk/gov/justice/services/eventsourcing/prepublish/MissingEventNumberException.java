package uk.gov.justice.services.eventsourcing.prepublish;

public class MissingEventNumberException extends RuntimeException {

    public MissingEventNumberException(final String message) {
        super(message);
    }
}
