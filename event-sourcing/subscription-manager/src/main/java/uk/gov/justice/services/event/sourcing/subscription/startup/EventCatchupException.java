package uk.gov.justice.services.event.sourcing.subscription.startup;

public class EventCatchupException extends RuntimeException {

    public EventCatchupException(final String message) {
        super(message);
    }
}
