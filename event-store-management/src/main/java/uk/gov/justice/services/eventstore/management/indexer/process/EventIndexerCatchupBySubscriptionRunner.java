package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class EventIndexerCatchupBySubscriptionRunner {

    @Inject
    private EventIndexerCatchupProcessorBean eventCatchupProcessorBean;

    @Resource
    private ManagedExecutorService managedExecutorService;

    public void runEventIndexerCatchupForSubscription(final Subscription subscription, final String componentName) {

        final EventIndexerCatchupTask eventCatchupTask = new EventIndexerCatchupTask(
                subscription,
                eventCatchupProcessorBean,
                componentName);

        managedExecutorService.submit(eventCatchupTask);
    }
}
