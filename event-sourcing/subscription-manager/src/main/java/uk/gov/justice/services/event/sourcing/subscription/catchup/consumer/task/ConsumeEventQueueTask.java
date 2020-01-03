package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumptionResolver;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.Queue;
import java.util.UUID;

public class ConsumeEventQueueTask implements Runnable {

    private final ConsumeEventQueueBean consumeEventQueueBean;
    private final Queue<PublishedEvent> events;
    private final String subscriptionName;
    private final CatchupCommand catchupCommand;
    private final UUID commandId;

    public ConsumeEventQueueTask(
            final ConsumeEventQueueBean consumeEventQueueBean,
            final Queue<PublishedEvent> events,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {
        this.consumeEventQueueBean = consumeEventQueueBean;
        this.events = events;
        this.subscriptionName = subscriptionName;
        this.catchupCommand = catchupCommand;
        this.commandId = commandId;
    }

    @Override
    public void run() {

        consumeEventQueueBean.consume(
                events,
                subscriptionName,
                catchupCommand,
                commandId
        );
    }
}
