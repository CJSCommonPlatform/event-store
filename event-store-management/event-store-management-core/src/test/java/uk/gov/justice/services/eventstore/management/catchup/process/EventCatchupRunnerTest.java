package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupRunnerTest {

    @Mock
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Mock
    private Event<CatchupStartedEvent> catchupStartedEventFirer;

    @Mock
    private SubscriptionCatchupProvider subscriptionCatchupProvider;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventCatchupRunner eventCatchupRunner;

    @Test
    public void shouldRunEventCatchupForEachSubscription() throws Exception {

        final UUID commandId = randomUUID();

        final SubscriptionCatchupDetails subscriptionCatchupDefinition_1 = mock(SubscriptionCatchupDetails.class);
        final SubscriptionCatchupDetails subscriptionCatchupDefinition_2 = mock(SubscriptionCatchupDetails.class);
        final List<SubscriptionCatchupDetails> subscriptionCatchupDefinitions = asList(subscriptionCatchupDefinition_1, subscriptionCatchupDefinition_2);

        final CatchupCommand catchupCommand = new EventCatchupCommand();

        when(subscriptionCatchupProvider.getBySubscription(catchupCommand)).thenReturn(subscriptionCatchupDefinitions);

        eventCatchupRunner.runEventCatchup(commandId, catchupCommand);

        final InOrder inOrder = inOrder(catchupStartedEventFirer, eventCatchupByComponentRunner);

        inOrder.verify(catchupStartedEventFirer).fire(new CatchupStartedEvent(
                commandId,
                catchupCommand,
                subscriptionCatchupDefinitions,
                clock.now()
        ));

        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(subscriptionCatchupDefinition_1, commandId, catchupCommand);
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(subscriptionCatchupDefinition_2, commandId, catchupCommand);
    }
}
