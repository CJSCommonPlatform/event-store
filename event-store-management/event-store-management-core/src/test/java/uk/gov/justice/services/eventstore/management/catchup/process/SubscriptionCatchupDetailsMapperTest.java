package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionCatchupDetailsMapperTest {

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

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));
        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_1.getEventSourceName()).thenReturn(eventSourceName_1);
        when(subscription_2.getName()).thenReturn(subscriptionName_2);
        when(subscription_2.getEventSourceName()).thenReturn(eventSourceName_2);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetails = subscriptionCatchupDetailsMapper.toSubscriptionCatchupDetails(subscriptionsDescriptor)
                .collect(toList());

        assertThat(subscriptionCatchupDetails.size(), is(2));

        assertThat(subscriptionCatchupDetails.get(0).getComponentName(), is(componentName));
        assertThat(subscriptionCatchupDetails.get(0).getSubscriptionName(), is(subscriptionName_1));
        assertThat(subscriptionCatchupDetails.get(0).getEventSourceName(), is(eventSourceName_1));
        assertThat(subscriptionCatchupDetails.get(1).getComponentName(), is(componentName));
        assertThat(subscriptionCatchupDetails.get(1).getSubscriptionName(), is(subscriptionName_2));
        assertThat(subscriptionCatchupDetails.get(1).getEventSourceName(), is(eventSourceName_2));
    }
}
