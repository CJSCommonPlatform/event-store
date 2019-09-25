package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupByComponentRunnerTest {

    @Mock
    private RunCatchupForComponentSelector runCatchupForComponentSelector;

    @Mock
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Test
    public void shouldGetAllSubscriptionsForTheComponentAndRunCatchupOnEach() throws Exception {

        final String componentName = "AN_EVENT_LISTENER";

        final String subscriptionName_1 = "subscriptionName_1";
        final String subscriptionName_2 = "subscriptionName_2";

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final CatchupCommand catchupCommand = new CatchupCommand();

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, EVENT_CATCHUP)).thenReturn(true);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_2.getName()).thenReturn(subscriptionName_2);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor, EVENT_CATCHUP, catchupCommand);

        final InOrder inOrder = inOrder(logger, eventCatchupProcessorBean);

        inOrder.verify(logger).info("Running EVENT_CATCHUP catchup for Component 'AN_EVENT_LISTENER', Subscription 'subscriptionName_1'");
        inOrder.verify(eventCatchupProcessorBean).performEventCatchup(new CatchupSubscriptionContext(componentName, subscription_1, EVENT_CATCHUP, catchupCommand));
        inOrder.verify(logger).info("Running EVENT_CATCHUP catchup for Component 'AN_EVENT_LISTENER', Subscription 'subscriptionName_2'");
        inOrder.verify(eventCatchupProcessorBean).performEventCatchup(new CatchupSubscriptionContext(componentName, subscription_2, EVENT_CATCHUP, catchupCommand));
    }

    @Test
    public void shouldNotRunCatchupForThisComponentIfTheComponentShouldNotBeRunForThisCatchupType() throws Exception {

        final String componentName = "AN_EVENT_PROCESSOR";
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final CatchupCommand catchupCommand = new CatchupCommand();

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, EVENT_CATCHUP)).thenReturn(false);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor, EVENT_CATCHUP, catchupCommand);

        verifyZeroInteractions(eventCatchupProcessorBean);
    }
}
