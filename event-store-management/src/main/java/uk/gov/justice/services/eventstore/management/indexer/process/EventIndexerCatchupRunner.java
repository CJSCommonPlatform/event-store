package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventIndexerCatchupRunner {

    @Inject
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Inject
    private Event<IndexerCatchupStartedEvent> catchupStartedEventFirer;

    @Inject
    private EventIndexerCatchupByComponentRunner eventCatchupByComponentRunner;

    @Inject
    private UtcClock clock;

    public void runEventCatchup() {
        catchupStartedEventFirer.fire(new IndexerCatchupStartedEvent(clock.now()));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(eventCatchupByComponentRunner::runEventIndexerCatchupForComponent);
    }
}
