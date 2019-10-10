package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.util.UUID;

/**
 * Interface for managing the consuming of JsonEnvelope events from Stream of events.
 *
 * This is used when replaying events at startup.  Different implementations can be constructed
 * according to how the events should be processed.
 */
public interface EventStreamConsumerManager {

    /**
     * Add an JsonEnvelope publishedEvent to the EventStreamConsumerManager
     *
     * @param publishedEvent - the JsonEnvelope to be consumed
     *
     * @return The number of events added to the stream. Note this is always one and is used
     *         to count the number of events consumed
     */
    int add(final PublishedEvent publishedEvent, final String subscriptionName, final CatchupCommand catchupCommand, final UUID commandId);

    void waitForCompletion();
}
