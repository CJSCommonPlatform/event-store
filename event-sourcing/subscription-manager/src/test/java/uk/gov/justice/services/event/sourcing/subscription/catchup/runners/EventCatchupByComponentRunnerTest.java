package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupByComponentRunnerTest {

    @Mock
    private EventCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

    @InjectMocks
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Test
    public void shouldGetAllSubscriptionsForTheComponentAndRunCatchupOnEach() throws Exception {

        final String componentName = "AN_EVENT_LISTENER";

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor);

        verify(eventCatchupBySubscriptionRunner).runEventCatchupForSubscription(subscription_1, componentName);
        verify(eventCatchupBySubscriptionRunner).runEventCatchupForSubscription(subscription_2, componentName);
    }

    @Test
    public void shouldNotRunCatchupForThisComponentIfTheComponentIsNotAnEventListener() throws Exception {

        final String componentName = "AN_EVENT_PROCESSOR";
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);

        eventCatchupByComponentRunner.runEventCatchupForComponent(subscriptionsDescriptor);

        verifyZeroInteractions(eventCatchupBySubscriptionRunner);
    }
}
