package uk.gov.justice.services.event.sourcing.subscription.startup.manager;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Interface for managing the consuming of JsonEnvelope events from Stream of events.
 *
 * This is used when replaying events at startup.  Different implementations can be constructed
 * according to how the events should be processed.
 */
public interface EventStreamConsumerManager {

    /**
     * Add an JsonEnvelope event to the EventStreamConsumerManager
     *
     * @param event - the JsonEnvelope to be consumed
     */
    void add(final JsonEnvelope event);

}
