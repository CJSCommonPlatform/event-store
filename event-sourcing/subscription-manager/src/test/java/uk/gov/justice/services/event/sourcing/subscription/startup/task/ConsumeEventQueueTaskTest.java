package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumeEventQueueTaskTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallConsumeEventsUntilTheEventQueueConsumerReturnsTrue() throws Exception {

        final Queue<JsonEnvelope> events = mock(Queue.class);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);

        when(eventQueueConsumer.consumeEventQueue(events)).thenReturn(false, true);

        final ConsumeEventQueueTask consumeEventQueueTask = new ConsumeEventQueueTask(events, eventQueueConsumer);

        assertThat(consumeEventQueueTask.call(), is(true));

        verify(eventQueueConsumer, times(2)).consumeEventQueue(events);
    }
}
