package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
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
    private EventCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

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
        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_2.getName()).thenReturn(subscriptionName_2);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor, catchupRequestedEvent);

        final InOrder inOrder = inOrder(logger, eventCatchupBySubscriptionRunner);

        inOrder.verify(logger).info("Running catchup for Component 'AN_EVENT_LISTENER', Subscription 'subscriptionName_1'");
        inOrder.verify(eventCatchupBySubscriptionRunner).runEventCatchupForSubscription(new CatchupContext(componentName, subscription_1, catchupRequestedEvent));
        inOrder.verify(logger).info("Running catchup for Component 'AN_EVENT_LISTENER', Subscription 'subscriptionName_2'");
        inOrder.verify(eventCatchupBySubscriptionRunner).runEventCatchupForSubscription(new CatchupContext(componentName, subscription_2, catchupRequestedEvent));
    }

    @Test
    public void shouldNotRunCatchupForThisComponentIfTheComponentIsNotAnEventListener() throws Exception {

        final String componentName = "AN_EVENT_PROCESSOR";
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor, catchupRequestedEvent);

        verifyZeroInteractions(eventCatchupBySubscriptionRunner);
    }
}
