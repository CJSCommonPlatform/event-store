package uk.gov.justice.services.event.sourcing.subscription.catchup;

public class EventCatchupException extends RuntimeException {

    public EventCatchupException(final String message) {
        super(message);
    }
}
