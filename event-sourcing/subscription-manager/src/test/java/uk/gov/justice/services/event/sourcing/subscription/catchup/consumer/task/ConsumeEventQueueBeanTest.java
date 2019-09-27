package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsumeEventQueueBeanTest {

    @InjectMocks
    private ConsumeEventQueueBean consumeEventQueueBean;

    @Test
    public void shouldConsumeTheEventQueueUntilEventsConsumedIsTrue() throws Exception {

        final Queue<PublishedEvent> events = new ConcurrentLinkedQueue<>(singletonList(mock(PublishedEvent.class))) ;
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);
        final String subscriptionName = "subscriptionName";
        final CatchupType catchupType = EVENT_CATCHUP;

        when(eventQueueConsumer.consumeEventQueue(events, subscriptionName, catchupType)).thenReturn(false, false, true);

        consumeEventQueueBean.consume(
                events,
                eventQueueConsumer,
                subscriptionName,
                catchupType
        );

        verify(eventQueueConsumer, times(3)).consumeEventQueue(events, subscriptionName, catchupType);
    }
}
