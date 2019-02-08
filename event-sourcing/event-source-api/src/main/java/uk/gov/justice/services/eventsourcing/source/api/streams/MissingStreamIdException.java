package uk.gov.justice.services.eventsourcing.source.api.streams;

public class MissingStreamIdException extends RuntimeException {

    public MissingStreamIdException(final String message) {
        super(message);
    }

    public MissingStreamIdException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
