package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class EventIndexerCatchupRunner {

    @Inject
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Inject
    private Event<IndexerCatchupStartedEvent> indexerCatchupStartedEventFirer;

    @Inject
    private EventIndexerCatchupByComponentRunner eventIndexerCatchupByComponentRunner;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void runEventIndexerCatchup(final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent) {

        logger.info("Received IndexerCatchupRequestedEvent");

        indexerCatchupStartedEventFirer.fire(new IndexerCatchupStartedEvent(clock.now()));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(subscriptionsDescriptor -> eventIndexerCatchupByComponentRunner.runEventIndexerCatchupForComponent(
                subscriptionsDescriptor, indexerCatchupRequestedEvent));
    }
}
