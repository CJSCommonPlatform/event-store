package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupRunnerTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Mock
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @InjectMocks
    private EventCatchupRunner eventCatchupRunner;

    @Test
    public void shouldRunEventCatchupForEachSubscription() throws Exception {

        final UUID commandId = randomUUID();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);

        final CatchupCommand catchupCommand = new EventCatchupCommand();

        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(asList(subscriptionsDescriptor_1, subscriptionsDescriptor_2));

        eventCatchupRunner.runEventCatchup(commandId, catchupCommand);

        final InOrder inOrder = inOrder(eventCatchupByComponentRunner);

        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(commandId, subscriptionsDescriptor_1, catchupCommand);
        inOrder.verify(eventCatchupByComponentRunner).runEventCatchupForComponent(commandId, subscriptionsDescriptor_2, catchupCommand);
    }
}
