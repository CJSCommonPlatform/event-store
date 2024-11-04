package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

public class AggregateSnapshotGenerationFailedException extends RuntimeException {

    public AggregateSnapshotGenerationFailedException(final String message) {
        super(message);
    }

    public AggregateSnapshotGenerationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
