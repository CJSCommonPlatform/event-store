package uk.gov.justice.services.eventsourcing.linkedevent;

public class LinkedEventSQLException extends RuntimeException {

    public LinkedEventSQLException(final String message) {
        super(message);
    }

    public LinkedEventSQLException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
