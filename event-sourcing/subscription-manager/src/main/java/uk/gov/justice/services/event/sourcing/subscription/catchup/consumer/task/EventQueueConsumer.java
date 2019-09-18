package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.lang.String.format;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.FinishedProcessingMessage;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;

import org.slf4j.Logger;

public class EventQueueConsumer {

    private final TransactionalEventProcessor transactionalEventProcessor;
    private final EventStreamConsumptionResolver eventStreamConsumptionResolver;
    private final Logger logger;

    public EventQueueConsumer(
            final TransactionalEventProcessor transactionalEventProcessor,
            final EventStreamConsumptionResolver eventStreamConsumptionResolver,
            final Logger logger) {
        this.transactionalEventProcessor = transactionalEventProcessor;
        this.eventStreamConsumptionResolver = eventStreamConsumptionResolver;
        this.logger = logger;
    }

    public boolean consumeEventQueue(final Queue<PublishedEvent> events, final String subscriptionName) {
        while (!events.isEmpty()) {
            final PublishedEvent publishedEvent = events.poll();

            try {
                transactionalEventProcessor.processWithEventBuffer(publishedEvent, subscriptionName);
            } catch (final RuntimeException e) {
                final String message = format("Failed to process publishedEvent with metadata: %s", publishedEvent.getMetadata());
                logger.error(
                        message,
                        e);
            }
        }

        return eventStreamConsumptionResolver.isEventConsumptionComplete(new FinishedProcessingMessage(events));
    }
}
