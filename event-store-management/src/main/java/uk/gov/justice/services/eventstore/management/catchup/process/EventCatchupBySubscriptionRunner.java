package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class EventCatchupBySubscriptionRunner {

    @Inject
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Resource
    private ManagedExecutorService managedExecutorService;

    public void runEventCatchupForSubscription(final Subscription subscription, final String componentName) {

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                subscription,
                eventCatchupProcessorBean,
                componentName);

        managedExecutorService.submit(eventCatchupTask);
    }
}
