package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;

import java.util.Queue;
import java.util.UUID;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumeEventQueueTaskManagerTest {

    @Mock
    private ManagedExecutorService managedExecutorService;

    @Mock
    private ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory;

    @InjectMocks
    private ConsumeEventQueueTaskManager consumeEventQueueTaskManager;

    @Test
    public void shouldAsynchronouslyRunConsumeEventQueue() throws Exception {

        final Queue<PublishedEvent> events = mock(Queue.class);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);
        final String subscriptionName = "subscription name";
        final CatchupCommand catchupCommand = new EventCatchupCommand();
        final UUID commandId = randomUUID();

        final ConsumeEventQueueTask consumeEventQueueTask = mock(ConsumeEventQueueTask.class);

        when(consumeEventQueueTaskFactory.createConsumeEventQueueTask(
                events,
                eventQueueConsumer,
                subscriptionName,
                catchupCommand,
                commandId
        )).thenReturn(consumeEventQueueTask);

        consumeEventQueueTaskManager.consume(
                events,
                eventQueueConsumer,
                subscriptionName,
                catchupCommand,
                commandId
        );

        verify(managedExecutorService).execute(consumeEventQueueTask);
    }
}
