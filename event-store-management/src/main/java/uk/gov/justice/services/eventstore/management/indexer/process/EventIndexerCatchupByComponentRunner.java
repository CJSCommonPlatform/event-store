package uk.gov.justice.services.eventstore.management.indexer.process;

import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import javax.inject.Inject;

public class EventIndexerCatchupByComponentRunner {

    @Inject
    EventIndexerCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

    public void runEventIndexerCatchupForComponent(final SubscriptionsDescriptor subscriptionsDescriptor) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (componentName.contains(EVENT_INDEXER)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> eventCatchupBySubscriptionRunner.runEventIndexerCatchupForSubscription(subscription, componentName));
        }
    }
}
