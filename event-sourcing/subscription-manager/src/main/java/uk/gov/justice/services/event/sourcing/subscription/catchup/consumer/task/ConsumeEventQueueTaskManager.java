package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.Queue;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class ConsumeEventQueueTaskManager {

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory;

    public void consume(
            final Queue<PublishedEvent> events,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {


        final ConsumeEventQueueTask consumeEventQueueTask = consumeEventQueueTaskFactory.createConsumeEventQueueTask(
                events,
                subscriptionName,
                catchupCommand,
                commandId
        );

        managedExecutorService.execute(consumeEventQueueTask);
    }
}
