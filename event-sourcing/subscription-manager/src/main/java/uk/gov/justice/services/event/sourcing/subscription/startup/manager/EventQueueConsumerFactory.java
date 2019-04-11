package uk.gov.justice.services.event.sourcing.subscription.startup.manager;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.EventQueueConsumer;

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
