package uk.gov.justice.services.eventstore.management.indexer.process;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
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
import javax.inject.Inject;
import javax.transaction.Transactional;

public class EventIndexerCatchupProcessor {

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private ConcurrentEventStreamConsumerManager concurrentEventStreamConsumerManager;

    @Inject
    private Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchupCompletedForSubscriptionEventFirer;

    @Inject
    private UtcClock clock;

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
        final int totalEventsProcessed = events.mapToInt(event -> concurrentEventStreamConsumerManager.add(event, subscriptionName)).sum();

        concurrentEventStreamConsumerManager.waitForCompletion();

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
