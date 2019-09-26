package uk.gov.justice.services.eventstore.management.shuttering.observers;

public class ShutteringException extends RuntimeException {

    public ShutteringException(final String message) {
        super(message);
    }
}
