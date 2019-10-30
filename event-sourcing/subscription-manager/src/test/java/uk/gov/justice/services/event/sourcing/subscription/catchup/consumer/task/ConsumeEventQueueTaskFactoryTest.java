package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;

import java.util.Queue;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ConsumeEventQueueTaskFactoryTest {

    @Inject
    private ConsumeEventQueueBean consumeEventQueueBean;

    @InjectMocks
    private ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory;

    @Test
    public void shouldCreateConsumeEventQueueTask() throws Exception {

        final Queue<PublishedEvent> events = mock(Queue.class);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);
        final String subscriptionName = "subscription name";
        final CatchupCommand catchupCommand = new EventCatchupCommand();
        final UUID commandId = randomUUID();

        final ConsumeEventQueueTask consumeEventQueueTask = consumeEventQueueTaskFactory.createConsumeEventQueueTask(
                events,
                eventQueueConsumer,
                subscriptionName,
                catchupCommand,
                commandId
        );

        assertThat(getValueOfField(consumeEventQueueTask, "consumeEventQueueBean", ConsumeEventQueueBean.class), is(consumeEventQueueBean));
        assertThat(getValueOfField(consumeEventQueueTask, "events", Queue.class), is(events));
        assertThat(getValueOfField(consumeEventQueueTask, "eventQueueConsumer", EventQueueConsumer.class), is(eventQueueConsumer));
        assertThat(getValueOfField(consumeEventQueueTask, "subscriptionName", String.class), is(subscriptionName));
        assertThat(getValueOfField(consumeEventQueueTask, "catchupCommand", CatchupCommand.class), is(catchupCommand));
        assertThat(getValueOfField(consumeEventQueueTask, "commandId", UUID.class), is(commandId));
    }
}