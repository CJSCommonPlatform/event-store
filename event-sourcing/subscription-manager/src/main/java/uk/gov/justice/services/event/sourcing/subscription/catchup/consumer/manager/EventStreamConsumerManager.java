package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

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
     * @param publishedEvent - the @See JsonEnvelope to be consumed
     * @param subscriptionName - the name of the subscription that is catching up
     * @param catchupCommand - the Catchup command name
     * @param commandId - the id of the command that ran the catchup
     *
     * @return The number of events added to the stream. Note this is always one and is used
     *         to count the number of events consumed
     */
    int add(final PublishedEvent publishedEvent, final String subscriptionName, final CatchupCommand catchupCommand, final UUID commandId);

    /**
     * Blocking method that will {@code Thread.wait()}  until all events are published
     */
    void waitForCompletion();
}
