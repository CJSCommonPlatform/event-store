package uk.gov.justice.services.eventsourcing.util.sql.triggers;

public class TriggerManipulationFailedException extends RuntimeException {

    public TriggerManipulationFailedException(final String message) {
        super(message);
    }

    public TriggerManipulationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
