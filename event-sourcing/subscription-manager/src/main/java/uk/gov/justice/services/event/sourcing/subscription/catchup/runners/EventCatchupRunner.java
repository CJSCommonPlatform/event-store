package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupStartedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupRunner {

    @Inject
    SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Inject
    Event<CatchupStartedEvent> catchupStartedEventFirer;

    @Inject
    EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Inject
    UtcClock clock;

    public void runEventCatchup() {
        catchupStartedEventFirer.fire(new CatchupStartedEvent(clock.now()));

        final List<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(eventCatchupByComponentRunner::runEventCatchupForComponent);
    }
}
