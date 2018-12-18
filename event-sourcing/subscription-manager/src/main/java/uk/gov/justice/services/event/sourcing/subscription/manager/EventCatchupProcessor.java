package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.slf4j.Logger;

public class EventCatchupProcessor {

    private final Subscription subscription;
    private final SubscriptionsRepository subscriptionsRepository;
    private final EventSource eventSource;
    private final TransactionalEventProcessor transactionalEventProcessor;
    private final Logger logger;

    public EventCatchupProcessor(
            final Subscription subscription,
            final EventSource eventSource,
            final SubscriptionsRepository subscriptionsRepository,
            final TransactionalEventProcessor transactionalEventProcessor,
            final Logger logger) {
        this.subscription = subscription;
        this.subscriptionsRepository = subscriptionsRepository;
        this.eventSource = eventSource;
        this.transactionalEventProcessor = transactionalEventProcessor;
        this.logger = logger;
    }

    public void performEventCatchup() {

        logger.info("Event catchup started");
        logger.info("Performing catchup of events...");
        final long eventNumber = subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscription.getName());

        final int totalEventsProcessed = eventSource.findEventsSince(eventNumber)
                .mapToInt(transactionalEventProcessor::processWithEventBuffer)
                .sum();

        logger.info(format("Event catchup retrieved and processed %d new events", totalEventsProcessed));
        logger.info("Event catchup complete");
    }


}
