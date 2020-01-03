package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;

import java.util.Queue;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumeEventQueueTaskTest {

    @Test
    public void shouldCallTheConsumeEventQueueBean() throws Exception {

        final ConsumeEventQueueBean consumeEventQueueBean = mock(ConsumeEventQueueBean.class);
        final Queue<PublishedEvent> events = mock(Queue.class);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);
        final String subscriptionName = "subscription name";
        final CatchupCommand catchupCommand = new EventCatchupCommand();
        final UUID commandId = randomUUID();

        final ConsumeEventQueueTask consumeEventQueueTask = new ConsumeEventQueueTask(
                consumeEventQueueBean,
                events,
                subscriptionName,
                catchupCommand,
                commandId
        );

        consumeEventQueueTask.run();

        verify(consumeEventQueueBean).consume(
                events,
                subscriptionName,
                catchupCommand,
                commandId
        );
    }
}
