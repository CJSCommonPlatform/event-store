package uk.gov.justice.services.eventstore.management.indexer.process;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import javax.enterprise.event.Event;
import javax.transaction.Transactional;

public class EventIndexerCatchupProcessor {

    private final ProcessedEventTrackingService processedEventTrackingService;
    private final PublishedEventSourceProvider publishedEventSourceProvider;
    private final EventStreamConsumerManager eventStreamConsumerManager;
    private final Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer;
    private final Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchupCompletedForSubscriptionEventFirer;
    private final UtcClock clock;

    public EventIndexerCatchupProcessor(
            final ProcessedEventTrackingService processedEventTrackingService,
            final PublishedEventSourceProvider publishedEventSourceProvider,
            final EventStreamConsumerManager eventStreamConsumerManager,
            final Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer,
            final Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchupCompletedForSubscriptionEventFirer,
            final UtcClock clock) {
        this.processedEventTrackingService = processedEventTrackingService;
        this.publishedEventSourceProvider = publishedEventSourceProvider;
        this.eventStreamConsumerManager = eventStreamConsumerManager;
        this.indexerCatchupStartedForSubscriptionEventFirer = indexerCatchupStartedForSubscriptionEventFirer;
        this.indexerCatchupCompletedForSubscriptionEventFirer = indexerCatchupCompletedForSubscriptionEventFirer;
        this.clock = clock;
    }

    @Transactional(NOT_SUPPORTED)
    public void performEventIndexerCatchup(final IndexerCatchupContext indexerCatchupContext) {

        final Subscription subscription = indexerCatchupContext.getSubscription();
        final String subscriptionName = subscription.getName();
        final String eventSourceName = subscription.getEventSourceName();
        final String componentName = indexerCatchupContext.getComponentName();
        final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent = indexerCatchupContext.getIndexerCatchupRequestedEvent();

        final PublishedEventSource eventSource = publishedEventSourceProvider.getPublishedEventSource(eventSourceName);
        final Long latestProcessedEventNumber = processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName);

        indexerCatchupStartedForSubscriptionEventFirer.fire(new IndexerCatchupStartedForSubscriptionEvent(
                subscriptionName,
                clock.now()));

        final Stream<PublishedEvent> events = eventSource.findEventsSince(latestProcessedEventNumber);
        final int totalEventsProcessed = events.mapToInt(event -> eventStreamConsumerManager.add(event, subscriptionName)).sum();

        eventStreamConsumerManager.waitForCompletion();

        final IndexerCatchupCompletedForSubscriptionEvent event = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                indexerCatchupRequestedEvent.getTarget(),
                clock.now(),
                totalEventsProcessed);

        indexerCatchupCompletedForSubscriptionEventFirer.fire(event);
    }
}
