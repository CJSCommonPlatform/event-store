package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

public class MissingEventNumberException extends RuntimeException {

    public MissingEventNumberException(final String message) {
        super(message);
    }
}
