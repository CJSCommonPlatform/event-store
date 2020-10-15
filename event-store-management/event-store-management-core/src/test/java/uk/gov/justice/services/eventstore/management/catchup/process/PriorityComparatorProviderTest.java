package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PriorityComparatorProviderTest {

    @InjectMocks
    private PriorityComparatorProvider priorityComparatorProvider;

    @Test
    public void shouldSortByComponentPriority() throws Exception {

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_3 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_4 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_5 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_6 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_7 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_8 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_9 = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor_1.getPrioritisation()).thenReturn(1);
        when(subscriptionsDescriptor_2.getPrioritisation()).thenReturn(2);
        when(subscriptionsDescriptor_3.getPrioritisation()).thenReturn(3);
        when(subscriptionsDescriptor_4.getPrioritisation()).thenReturn(4);
        when(subscriptionsDescriptor_5.getPrioritisation()).thenReturn(5);
        when(subscriptionsDescriptor_6.getPrioritisation()).thenReturn(6);
        when(subscriptionsDescriptor_7.getPrioritisation()).thenReturn(7);
        when(subscriptionsDescriptor_8.getPrioritisation()).thenReturn(8);
        when(subscriptionsDescriptor_9.getPrioritisation()).thenReturn(9);

        final List<SubscriptionsDescriptor> subscriptionsDescriptors = asList(
                subscriptionsDescriptor_3,
                subscriptionsDescriptor_8,
                subscriptionsDescriptor_4,
                subscriptionsDescriptor_2,
                subscriptionsDescriptor_9,
                subscriptionsDescriptor_5,
                subscriptionsDescriptor_7,
                subscriptionsDescriptor_6,
                subscriptionsDescriptor_1
        );

        subscriptionsDescriptors.sort(priorityComparatorProvider.getSubscriptionDescriptorComparator());

        assertThat(subscriptionsDescriptors.get(0).getPrioritisation(), is(1));
        assertThat(subscriptionsDescriptors.get(1).getPrioritisation(), is(2));
        assertThat(subscriptionsDescriptors.get(2).getPrioritisation(), is(3));
        assertThat(subscriptionsDescriptors.get(3).getPrioritisation(), is(4));
        assertThat(subscriptionsDescriptors.get(4).getPrioritisation(), is(5));
        assertThat(subscriptionsDescriptors.get(5).getPrioritisation(), is(6));
        assertThat(subscriptionsDescriptors.get(6).getPrioritisation(), is(7));
        assertThat(subscriptionsDescriptors.get(7).getPrioritisation(), is(8));
        assertThat(subscriptionsDescriptors.get(8).getPrioritisation(), is(9));
    }

    @Test
    public void shouldSortBySubscriptionPriority() throws Exception {

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final Subscription subscription_3 = mock(Subscription.class);
        final Subscription subscription_4 = mock(Subscription.class);
        final Subscription subscription_5 = mock(Subscription.class);
        final Subscription subscription_6 = mock(Subscription.class);
        final Subscription subscription_7 = mock(Subscription.class);
        final Subscription subscription_8 = mock(Subscription.class);
        final Subscription subscription_9 = mock(Subscription.class);

        when(subscription_1.getPrioritisation()).thenReturn(1);
        when(subscription_2.getPrioritisation()).thenReturn(2);
        when(subscription_3.getPrioritisation()).thenReturn(3);
        when(subscription_4.getPrioritisation()).thenReturn(4);
        when(subscription_5.getPrioritisation()).thenReturn(5);
        when(subscription_6.getPrioritisation()).thenReturn(6);
        when(subscription_7.getPrioritisation()).thenReturn(7);
        when(subscription_8.getPrioritisation()).thenReturn(8);
        when(subscription_9.getPrioritisation()).thenReturn(9);

        final List<Subscription> subscriptions = asList(
                subscription_3,
                subscription_8,
                subscription_4,
                subscription_2,
                subscription_9,
                subscription_5,
                subscription_7,
                subscription_6,
                subscription_1
        );

        subscriptions.sort(priorityComparatorProvider.getSubscriptionComparator());

        assertThat(subscriptions.get(0).getPrioritisation(), is(1));
        assertThat(subscriptions.get(1).getPrioritisation(), is(2));
        assertThat(subscriptions.get(2).getPrioritisation(), is(3));
        assertThat(subscriptions.get(3).getPrioritisation(), is(4));
        assertThat(subscriptions.get(4).getPrioritisation(), is(5));
        assertThat(subscriptions.get(5).getPrioritisation(), is(6));
        assertThat(subscriptions.get(6).getPrioritisation(), is(7));
        assertThat(subscriptions.get(7).getPrioritisation(), is(8));
        assertThat(subscriptions.get(8).getPrioritisation(), is(9));
    }
}
