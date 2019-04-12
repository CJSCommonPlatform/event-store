package uk.gov.justice.services.eventsourcing.publishedevent;

public class PublishedEventSQLException extends RuntimeException {

    public PublishedEventSQLException(final String message) {
        super(message);
    }

    public PublishedEventSQLException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
