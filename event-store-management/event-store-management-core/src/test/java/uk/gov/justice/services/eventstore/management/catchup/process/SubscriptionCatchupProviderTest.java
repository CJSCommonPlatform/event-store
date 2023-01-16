package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionCatchupProviderTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Mock
    private CanCatchupFilter canCatchupFilter;

    @Spy
    private PriorityComparatorProvider priorityComparatorProvider = new PriorityComparatorProvider();

    @Mock
    private SubscriptionCatchupDetailsMapper subscriptionCatchupDetailsMapper;

    @InjectMocks
    private SubscriptionCatchupProvider subscriptionCatchupProvider;

    @Test
    public void shouldFindSubscriptionsToCatchup() throws Exception {

        final CatchupCommand catchupCommand = new IndexerCatchupCommand();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_2 = mock(SubscriptionsDescriptor.class);
        final SubscriptionsDescriptor subscriptionsDescriptor_3 = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor_1.getPrioritisation()).thenReturn(20);
        when(subscriptionsDescriptor_3.getPrioritisation()).thenReturn(60);

        final SubscriptionCatchupDetails subscriptionCatchupDetails_1_1 = mock(SubscriptionCatchupDetails.class);
        final SubscriptionCatchupDetails subscriptionCatchupDetails_1_2 = mock(SubscriptionCatchupDetails.class);
        final SubscriptionCatchupDetails subscriptionCatchupDetails_3_1 = mock(SubscriptionCatchupDetails.class);
        final SubscriptionCatchupDetails subscriptionCatchupDetails_3_2 = mock(SubscriptionCatchupDetails.class);

        when(subscriptionsDescriptorsRegistry.getAll()).thenReturn(asList(
                subscriptionsDescriptor_3,
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        ));
        when(canCatchupFilter.canCatchup(subscriptionsDescriptor_1, catchupCommand)).thenReturn(true);
        when(canCatchupFilter.canCatchup(subscriptionsDescriptor_2, catchupCommand)).thenReturn(false);
        when(canCatchupFilter.canCatchup(subscriptionsDescriptor_3, catchupCommand)).thenReturn(true);
        when(subscriptionCatchupDetailsMapper.toSubscriptionCatchupDetails(subscriptionsDescriptor_1)).thenReturn(Stream.of(subscriptionCatchupDetails_1_1, subscriptionCatchupDetails_1_2));
        when(subscriptionCatchupDetailsMapper.toSubscriptionCatchupDetails(subscriptionsDescriptor_3)).thenReturn(Stream.of(subscriptionCatchupDetails_3_1, subscriptionCatchupDetails_3_2));

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetails = subscriptionCatchupProvider.getBySubscription(catchupCommand);

        assertThat(subscriptionCatchupDetails.size(), is(4));
        assertThat(subscriptionCatchupDetails.get(0), is(subscriptionCatchupDetails_1_1));
        assertThat(subscriptionCatchupDetails.get(1), is(subscriptionCatchupDetails_1_2));
        assertThat(subscriptionCatchupDetails.get(2), is(subscriptionCatchupDetails_3_1));
        assertThat(subscriptionCatchupDetails.get(3), is(subscriptionCatchupDetails_3_2));
    }
}
