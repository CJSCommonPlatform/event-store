package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventStreamConsumerManagerFactory;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventIndexerCatchupProcessorFactory {

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchupCompletedForSubscriptionEventFirer;

    @Inject
    private EventStreamConsumerManagerFactory eventStreamConsumerManagerFactory;

    @Inject
    private UtcClock clock;

    public EventIndexerCatchupProcessor create() {

        final EventStreamConsumerManager eventStreamConsumerManager = eventStreamConsumerManagerFactory.create();

        return new EventIndexerCatchupProcessor(
                processedEventTrackingService,
                publishedEventSourceProvider,
                eventStreamConsumerManager,
                indexerCatchupStartedForSubscriptionEventFirer,
                indexerCatchupCompletedForSubscriptionEventFirer,
                clock
        );
    }
}
