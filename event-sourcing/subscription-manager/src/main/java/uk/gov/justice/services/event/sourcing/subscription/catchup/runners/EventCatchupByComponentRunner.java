package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import javax.inject.Inject;

public class EventCatchupByComponentRunner {

    @Inject
    EventCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

    public void runEventCatchupForComponent(final SubscriptionsDescriptor subscriptionsDescriptor) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (componentName.contains(EVENT_LISTENER)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> eventCatchupBySubscriptionRunner.runEventCatchupForSubscription(subscription, componentName));
        }
    }
}
