package uk.gov.justice.services.eventsourcing.publishedevent;

public class PublishQueueException extends RuntimeException {

    public PublishQueueException(final String message) {
        super(message);
    }

    public PublishQueueException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
