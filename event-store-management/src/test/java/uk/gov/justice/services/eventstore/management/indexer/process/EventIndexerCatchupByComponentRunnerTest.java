package uk.gov.justice.services.eventstore.management.indexer.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
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
public class EventIndexerCatchupByComponentRunnerTest {
    @Mock
    private EventIndexerCatchupBySubscriptionRunner eventIndexerCatchupBySubscriptionRunner;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventIndexerCatchupByComponentRunner eventIndexerCatchupByComponentRunner;

    @Test
    public void shouldGetAllSubscriptionsForTheComponentAndRunCatchupOnEach() throws Exception {

        final String componentName = "AN_EVENT_INDEXER";

        final String subscriptionName_1 = "subscriptionName_1";
        final String subscriptionName_2 = "subscriptionName_2";

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final IndexerCatchupRequestedEvent catchupRequestedEvent = mock(IndexerCatchupRequestedEvent.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(subscriptionsDescriptor.getSubscriptions()).thenReturn(asList(subscription_1, subscription_2));

        when(subscription_1.getName()).thenReturn(subscriptionName_1);
        when(subscription_2.getName()).thenReturn(subscriptionName_2);

        eventIndexerCatchupByComponentRunner.runEventIndexerCatchupForComponent(subscriptionsDescriptor, catchupRequestedEvent);

        final InOrder inOrder = inOrder(logger, eventIndexerCatchupBySubscriptionRunner);

        inOrder.verify(logger).info("Running catchup for Component 'AN_EVENT_INDEXER', Subscription 'subscriptionName_1'");
        inOrder.verify(eventIndexerCatchupBySubscriptionRunner).runEventIndexerCatchupForSubscription(new IndexerCatchupContext(componentName, subscription_1, catchupRequestedEvent));
        inOrder.verify(logger).info("Running catchup for Component 'AN_EVENT_INDEXER', Subscription 'subscriptionName_2'");
        inOrder.verify(eventIndexerCatchupBySubscriptionRunner).runEventIndexerCatchupForSubscription(new IndexerCatchupContext(componentName, subscription_2, catchupRequestedEvent));
    }

    @Test
    public void shouldNotRunCatchupForThisComponentIfTheComponentIsNotAnEventListener() throws Exception {

        final String componentName = "AN_EVENT_PROCESSOR";
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);
        final IndexerCatchupRequestedEvent catchupRequestedEvent = mock(IndexerCatchupRequestedEvent.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);

        eventIndexerCatchupByComponentRunner.runEventIndexerCatchupForComponent(subscriptionsDescriptor, catchupRequestedEvent);

        verifyZeroInteractions(eventIndexerCatchupBySubscriptionRunner);
    }
}
