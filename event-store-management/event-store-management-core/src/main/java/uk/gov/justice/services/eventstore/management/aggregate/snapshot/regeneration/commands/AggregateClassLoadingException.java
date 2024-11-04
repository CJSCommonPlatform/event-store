package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

public class AggregateClassLoadingException extends RuntimeException {

    public AggregateClassLoadingException(final String message) {
        super(message);
    }

    public AggregateClassLoadingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
