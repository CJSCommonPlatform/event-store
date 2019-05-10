package uk.gov.justice.services.eventsourcing.publishedevent;

public class EventFetchingException extends RuntimeException {

    public EventFetchingException(final String message) {
        super(message);
    }

    public EventFetchingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
