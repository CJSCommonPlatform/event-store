package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.EventStreamConsumptionResolver;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;

public class ConsumeEventQueueTaskFactory {

    private final TransactionalEventProcessor transactionalEventProcessor;

    public ConsumeEventQueueTaskFactory(final TransactionalEventProcessor transactionalEventProcessor) {
        this.transactionalEventProcessor = transactionalEventProcessor;
    }

    public ConsumeEventQueueTask createWith(final Queue<JsonEnvelope> events,
                                            final EventStreamConsumptionResolver eventStreamConsumptionResolver) {

        final EventQueueConsumer eventQueueConsumer = new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                getLogger(EventQueueConsumer.class));

        return new ConsumeEventQueueTask(
                events,
                eventQueueConsumer);
    }
}
