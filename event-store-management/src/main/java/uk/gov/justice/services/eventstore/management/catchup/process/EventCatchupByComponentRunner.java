package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import javax.inject.Inject;

import org.slf4j.Logger;

public class EventCatchupByComponentRunner {

    @Inject
    private EventCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

    @Inject
    private Logger logger;

    public void runEventCatchupForComponent(final SubscriptionsDescriptor subscriptionsDescriptor, final CatchupRequestedEvent catchupRequestedEvent) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (componentName.contains(EVENT_LISTENER)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> runEventCatchupForSubscription(catchupRequestedEvent, componentName, subscription));
        }
    }

    private void runEventCatchupForSubscription(
            final CatchupRequestedEvent catchupRequestedEvent,
            final String componentName,
            final Subscription subscription) {

        logger.info(format("Running catchup for Component '%s', Subscription '%s'", componentName, subscription.getName()));

        final CatchupContext catchupContext = new CatchupContext(
                componentName,
                subscription,
                catchupRequestedEvent);

        eventCatchupBySubscriptionRunner.runEventCatchupForSubscription(catchupContext);
    }
}
