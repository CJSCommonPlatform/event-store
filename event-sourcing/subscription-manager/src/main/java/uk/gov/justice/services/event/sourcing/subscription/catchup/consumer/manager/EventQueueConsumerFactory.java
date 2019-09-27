package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventProcessingFailedHandler;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventQueueConsumer;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;

import javax.inject.Inject;

public class EventQueueConsumerFactory {

    @Inject
    private TransactionalEventProcessor transactionalEventProcessor;

    @Inject
    private EventProcessingFailedHandler eventProcessingFailedHandler;

    public EventQueueConsumer create(final EventStreamConsumptionResolver eventStreamConsumptionResolver) {
        return new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                eventProcessingFailedHandler);
    }
}
