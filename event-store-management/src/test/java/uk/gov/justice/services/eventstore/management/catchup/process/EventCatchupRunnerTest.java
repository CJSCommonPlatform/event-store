package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupRunnerTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Mock
    private Event<CatchupStartedEvent> catchupStartedEventFirer;

    @Mock
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupRunner eventCatchupRunner;

    @Test
    public void shouldRunEventCatchupForEachSubscription() throws Exception {

        final ZonedDateTime startTime = new UtcClock().now();
        final ZonedDateTime endTime = startTime.plusMinutes(23);

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);

        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        when(clock.now()).thenReturn(startTime, endTime);
        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        eventCatchupRunner.runEventCatchup(catchupRequestedEvent);

        final InOrder inOrder = inOrder(
                logger,
                catchupStartedEventFirer,
                eventCatchupByComponentRunner
        );

        inOrder.verify(logger).info("Received CatchupRequestedEvent");
        inOrder.verify(catchupStartedEventFirer).fire(new CatchupStartedEvent(startTime));
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(subscriptionsDescriptor_1, catchupRequestedEvent);
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(subscriptionsDescriptor_2, catchupRequestedEvent);
    }
}
