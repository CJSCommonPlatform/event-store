package uk.gov.justice.services.eventstore.management.indexer.process;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;

import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import javax.inject.Inject;

import org.slf4j.Logger;

public class EventIndexerCatchupByComponentRunner {
    @Inject
    private EventIndexerCatchupBySubscriptionRunner eventIndexerCatchupBySubscriptionRunner;

    @Inject
    private Logger logger;

    public void runEventIndexerCatchupForComponent(final SubscriptionsDescriptor subscriptionsDescriptor,
                                            final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (componentName.contains(EVENT_INDEXER)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> runEventIndexerCatchupForSubscription(indexerCatchupRequestedEvent, componentName, subscription));
        }
    }

    private void runEventIndexerCatchupForSubscription(
            final IndexerCatchupRequestedEvent catchupRequestedEvent,
            final String componentName,
            final Subscription subscription) {

        logger.info(format("Running catchup for Component '%s', Subscription '%s'", componentName, subscription.getName()));

        final IndexerCatchupContext catchupContext = new IndexerCatchupContext(
                componentName,
                subscription,
                catchupRequestedEvent);

        eventIndexerCatchupBySubscriptionRunner.runEventIndexerCatchupForSubscription(catchupContext);
    }
}
