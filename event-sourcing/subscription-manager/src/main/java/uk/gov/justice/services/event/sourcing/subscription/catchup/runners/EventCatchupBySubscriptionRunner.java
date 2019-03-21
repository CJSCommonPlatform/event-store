package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventCatchupProcessorBean;
import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventCatchupTask;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class EventCatchupBySubscriptionRunner {

    @Inject
    EventCatchupProcessorBean eventCatchupProcessorBean;

    @Resource
    ManagedExecutorService managedExecutorService;

    public void runEventCatchupForSubscription(final Subscription subscription, final String componentName) {

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                componentName,
                subscription,
                eventCatchupProcessorBean);

        managedExecutorService.submit(eventCatchupTask);
    }
}
