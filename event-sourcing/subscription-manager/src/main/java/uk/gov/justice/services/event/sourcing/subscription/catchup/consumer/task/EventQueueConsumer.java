package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.FinishedProcessingMessage;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.Queue;
import java.util.UUID;

public class EventQueueConsumer {

    private final TransactionalEventProcessor transactionalEventProcessor;
    private final EventStreamConsumptionResolver eventStreamConsumptionResolver;
    private final EventProcessingFailedHandler eventProcessingFailedHandler;

    public EventQueueConsumer(
            final TransactionalEventProcessor transactionalEventProcessor,
            final EventStreamConsumptionResolver eventStreamConsumptionResolver,
            final EventProcessingFailedHandler eventProcessingFailedHandler) {
        this.transactionalEventProcessor = transactionalEventProcessor;
        this.eventStreamConsumptionResolver = eventStreamConsumptionResolver;
        this.eventProcessingFailedHandler = eventProcessingFailedHandler;
    }

    public boolean consumeEventQueue(
            final UUID commandId,
            final Queue<PublishedEvent> events,
            final String subscriptionName,
            final CatchupType catchupType) {
        while (!events.isEmpty()) {
            final PublishedEvent publishedEvent = events.poll();

            try {
                transactionalEventProcessor.processWithEventBuffer(publishedEvent, subscriptionName);
            } catch (final RuntimeException e) {
                eventProcessingFailedHandler.handle(e, publishedEvent, subscriptionName, catchupType, commandId);
            }
        }

        return eventStreamConsumptionResolver.isEventConsumptionComplete(new FinishedProcessingMessage(events));
    }
}
