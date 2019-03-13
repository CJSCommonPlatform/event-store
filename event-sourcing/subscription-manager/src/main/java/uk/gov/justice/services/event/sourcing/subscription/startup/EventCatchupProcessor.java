package uk.gov.justice.services.event.sourcing.subscription.startup;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventCatchupProcessor {

    private final ProcessedEventTrackingService processedEventTrackingService;
    private final EventSourceProvider eventSourceProvider;
    private final EventStreamConsumerManager eventStreamConsumerManager;
    private final Logger logger;

    public EventCatchupProcessor(
            final ProcessedEventTrackingService processedEventTrackingService,
            final EventSourceProvider eventSourceProvider,
            final EventStreamConsumerManager eventStreamConsumerManager,
            final Logger logger) {
        this.processedEventTrackingService = processedEventTrackingService;
        this.eventSourceProvider = eventSourceProvider;
        this.eventStreamConsumerManager = eventStreamConsumerManager;
        this.logger = logger;
    }

    @Transactional(NOT_SUPPORTED)
    public void performEventCatchup(final Subscription subscription) {

        final EventSource eventSource = eventSourceProvider.getEventSource(subscription.getEventSourceName());

        logger.info("Event catchup started");
        logger.info("Performing catchup of events...");
        final Long latestProcessedEventNumber = processedEventTrackingService.getLatestProcessedEventNumber(subscription.getEventSourceName());

        final Stream<JsonEnvelope> events = eventSource.findEventsSince(latestProcessedEventNumber);
        final int totalEventsProcessed = events.mapToInt(this::process).sum();

        logger.info(format("Event catchup retrieved and processed %d new events", totalEventsProcessed));
        logger.info("Event catchup complete");
    }

    private int process(final JsonEnvelope event) {
        eventStreamConsumerManager.add(event);
        return 1;
    }
}
