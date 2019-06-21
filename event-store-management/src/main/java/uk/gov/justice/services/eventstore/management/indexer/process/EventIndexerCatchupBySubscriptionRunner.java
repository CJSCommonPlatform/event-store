package uk.gov.justice.services.eventstore.management.indexer.process;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class EventIndexerCatchupBySubscriptionRunner {

    @Inject
    private EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean;

    @Resource
    private ManagedExecutorService managedExecutorService;

    public void runEventIndexerCatchupForSubscription(final IndexerCatchupContext indexerCatchupContext) {

        final EventIndexerCatchupTask eventCatchupTask = new EventIndexerCatchupTask(indexerCatchupContext, eventIndexerCatchupProcessorBean);

        managedExecutorService.submit(eventCatchupTask);
    }
}
