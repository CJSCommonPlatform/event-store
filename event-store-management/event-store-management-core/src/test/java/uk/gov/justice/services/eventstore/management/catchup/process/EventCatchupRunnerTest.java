package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.time.ZonedDateTime;
import java.util.UUID;

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

        final UUID commandId = randomUUID();
        final ZonedDateTime startTime = new UtcClock().now();
        final ZonedDateTime endTime = startTime.plusMinutes(23);

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);

        final CatchupCommand catchupCommand = new EventCatchupCommand();

        when(clock.now()).thenReturn(startTime, endTime);
        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        eventCatchupRunner.runEventCatchup(commandId, catchupCommand);

        final InOrder inOrder = inOrder(
                logger,
                catchupStartedEventFirer,
                eventCatchupByComponentRunner
        );

        inOrder.verify(logger).info("Received CatchupRequestedEvent for CATCHUP");
        inOrder.verify(catchupStartedEventFirer).fire(new CatchupStartedEvent(commandId, catchupCommand, startTime));
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(commandId, subscriptionsDescriptor_1, catchupCommand);
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(commandId, subscriptionsDescriptor_2, catchupCommand);
    }
}
