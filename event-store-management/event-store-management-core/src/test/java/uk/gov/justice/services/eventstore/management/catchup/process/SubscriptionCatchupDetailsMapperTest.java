package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscriptionCatchupDetailsMapperTest {

    @Spy
    private PriorityComparatorProvider priorityComparatorProvider = new PriorityComparatorProvider();

    @Mock
    private EventSourceNameFilter eventSourceNameFilter;

    @InjectMocks
    private SubscriptionCatchupDetailsMapper subscriptionCatchupDetailsMapper;

    @Test
    public void shouldGetSubscriptionCatchupDetailsFromSubscriptionsDescriptor() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final String subscriptionName_1 = "subscriptionName_1";
        final String eventSourceName_1 = "eventSourceName_1";
        final String subscriptionName_2 = "subscriptionName_2";
        final String eventSourceName_2 = "eventSourceName_2";

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        when(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription_1)).thenReturn(true);
        when(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription_2)).thenReturn(true);
        when(subscription_1.getPrioritisation()).thenReturn(100);
        when(subscription_2.getPrioritisation()).thenReturn(200);
        
        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_2, subscription_1));
        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_1.getEventSourceName()).thenReturn(eventSourceName_1);
        when(subscription_2.getName()).thenReturn(subscriptionName_2);
        when(subscription_2.getEventSourceName()).thenReturn(eventSourceName_2);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetails = subscriptionCatchupDetailsMapper
                .toSubscriptionCatchupDetails(subscriptionsDescriptor)
                .toList();

        assertThat(subscriptionCatchupDetails.size(), is(2));

        assertThat(subscriptionCatchupDetails.get(0).getComponentName(), is(componentName));
        assertThat(subscriptionCatchupDetails.get(0).getSubscriptionName(), is(subscriptionName_1));
        assertThat(subscriptionCatchupDetails.get(0).getEventSourceName(), is(eventSourceName_1));
        assertThat(subscriptionCatchupDetails.get(1).getComponentName(), is(componentName));
        assertThat(subscriptionCatchupDetails.get(1).getSubscriptionName(), is(subscriptionName_2));
        assertThat(subscriptionCatchupDetails.get(1).getEventSourceName(), is(eventSourceName_2));
    }

    @Test
    public void shouldIgnoreEventSourcesThatAreNotMarkedAsWhitelisted() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final String subscriptionName_1 = "subscriptionName_1";
        final String eventSourceName_1 = "eventSourceName_1";

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        when(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription_1)).thenReturn(true);
        when(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription_2)).thenReturn(false);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_2, subscription_1));
        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_1.getEventSourceName()).thenReturn(eventSourceName_1);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetails = subscriptionCatchupDetailsMapper
                .toSubscriptionCatchupDetails(subscriptionsDescriptor)
                .toList();

        assertThat(subscriptionCatchupDetails.size(), is(1));

        assertThat(subscriptionCatchupDetails.get(0).getComponentName(), is(componentName));
        assertThat(subscriptionCatchupDetails.get(0).getSubscriptionName(), is(subscriptionName_1));
        assertThat(subscriptionCatchupDetails.get(0).getEventSourceName(), is(eventSourceName_1));
    }

}
