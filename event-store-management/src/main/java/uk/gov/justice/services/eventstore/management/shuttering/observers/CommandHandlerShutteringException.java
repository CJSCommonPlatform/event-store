package uk.gov.justice.services.eventstore.management.shuttering.observers;

public class CommandHandlerShutteringException extends RuntimeException {

    public CommandHandlerShutteringException(final String message) {
        super(message);
    }
}
