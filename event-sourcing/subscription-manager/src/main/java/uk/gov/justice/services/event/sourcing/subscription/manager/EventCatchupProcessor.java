package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventCatchupProcessor {

    private final SubscriptionsRepository subscriptionsRepository;
    private final EventSourceProvider eventSourceProvider;
    private final TransactionalEventProcessor transactionalEventProcessor;
    private final Logger logger;

    public EventCatchupProcessor(
            final SubscriptionsRepository subscriptionsRepository,
            final EventSourceProvider eventSourceProvider,
            final TransactionalEventProcessor transactionalEventProcessor,
            final Logger logger) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.eventSourceProvider = eventSourceProvider;
        this.transactionalEventProcessor = transactionalEventProcessor;
        this.logger = logger;
    }

    @Transactional(NOT_SUPPORTED)
    public void performEventCatchup(final Subscription subscription) {

        final EventSource eventSource = eventSourceProvider.getEventSource(subscription.getEventSourceName());

        logger.info("Event catchup started");
        logger.info("Performing catchup of events...");
        final long eventNumber = subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscription.getEventSourceName());

        final int totalEventsProcessed = eventSource.findEventsSince(eventNumber)
                .mapToInt(transactionalEventProcessor::processWithEventBuffer)
                .sum();

        logger.info(format("Event catchup retrieved and processed %d new events", totalEventsProcessed));
        logger.info("Event catchup complete");
    }
}
