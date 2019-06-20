package uk.gov.justice.services.eventstore.management.catchup.process;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class EventCatchupBySubscriptionRunner {

    @Inject
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Resource
    private ManagedExecutorService managedExecutorService;

    public void runEventCatchupForSubscription(final CatchupContext catchupContext) {

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                catchupContext,
                eventCatchupProcessorBean);

        managedExecutorService.submit(eventCatchupTask);
    }
}
