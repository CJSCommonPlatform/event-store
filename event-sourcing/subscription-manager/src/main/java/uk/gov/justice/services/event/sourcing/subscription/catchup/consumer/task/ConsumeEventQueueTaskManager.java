package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

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
            final EventQueueConsumer eventQueueConsumer,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {


        final ConsumeEventQueueTask consumeEventQueueTask = consumeEventQueueTaskFactory.createConsumeEventQueueTask(
                events,
                eventQueueConsumer,
                subscriptionName,
                catchupCommand,
                commandId
        );

        managedExecutorService.execute(consumeEventQueueTask);
    }
}
