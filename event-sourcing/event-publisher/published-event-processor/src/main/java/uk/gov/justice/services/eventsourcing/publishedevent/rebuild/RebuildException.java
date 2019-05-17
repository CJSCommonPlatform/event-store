package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

public class RebuildException extends RuntimeException {

    public RebuildException(final String message) {
        super(message);
    }

    public RebuildException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
