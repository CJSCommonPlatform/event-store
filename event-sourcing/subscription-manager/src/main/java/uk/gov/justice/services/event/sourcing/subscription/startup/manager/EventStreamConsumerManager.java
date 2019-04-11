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
     *
     * @return The number of events added to the stream. Note this is always one and is used
     *         to count the number of events consumed
     */
    int add(final JsonEnvelope event, final String subscriptionName);

    void waitForCompletion();
}
