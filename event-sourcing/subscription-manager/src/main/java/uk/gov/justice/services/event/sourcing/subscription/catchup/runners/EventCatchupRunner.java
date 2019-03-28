package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupRunner {

    @Inject
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Inject
    private Event<CatchupStartedEvent> catchupStartedEventFirer;

    @Inject
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Inject
    private UtcClock clock;

    public void runEventCatchup() {
        catchupStartedEventFirer.fire(new CatchupStartedEvent(clock.now()));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(eventCatchupByComponentRunner::runEventCatchupForComponent);
    }
}
