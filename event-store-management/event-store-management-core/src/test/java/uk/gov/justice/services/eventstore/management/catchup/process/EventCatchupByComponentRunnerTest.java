package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupByComponentRunnerTest {

    @Mock
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Test
    public void shouldGetAllSubscriptionsForTheComponentAndRunCatchupOnEach() throws Exception {

        final UUID commandId = randomUUID();
        final String componentName = "AN_EVENT_LISTENER";
        final String subscriptionName = "subscriptionName";

        final SubscriptionCatchupDetails subscriptionCatchupDefinition = mock(SubscriptionCatchupDetails.class);
        
        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();

        when(subscriptionCatchupDefinition.getComponentName()).thenReturn(componentName);
        when(subscriptionCatchupDefinition.getSubscriptionName()).thenReturn(subscriptionName);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionCatchupDefinition, commandId, eventCatchupCommand);

        final InOrder inOrder = inOrder(logger, eventCatchupProcessorBean);

        inOrder.verify(logger).info("Running CATCHUP for Component 'AN_EVENT_LISTENER', Subscription 'subscriptionName'");
        inOrder.verify(eventCatchupProcessorBean).performEventCatchup(new CatchupSubscriptionContext(commandId, componentName, subscriptionCatchupDefinition, eventCatchupCommand));
    }
}
