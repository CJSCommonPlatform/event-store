package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventQueueConsumer;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;

import javax.inject.Inject;

public class EventQueueConsumerFactory {

    @Inject
    private TransactionalEventProcessor transactionalEventProcessor;

    public EventQueueConsumer create(final EventStreamConsumptionResolver eventStreamConsumptionResolver) {
        return new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                getLogger(EventQueueConsumer.class));
    }
}
