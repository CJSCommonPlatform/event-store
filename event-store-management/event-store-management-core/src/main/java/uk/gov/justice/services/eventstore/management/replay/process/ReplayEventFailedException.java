package uk.gov.justice.services.eventstore.management.replay.process;

public class ReplayEventFailedException extends RuntimeException {

    public ReplayEventFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ReplayEventFailedException(final String message) {
        super(message);
    }
}
