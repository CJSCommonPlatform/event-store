package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventIndexerCatchupBySubscriptionRunnerTest {

    @Mock
    private EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean;

    @Mock
    private ManagedExecutorService managedExecutorService;

    @InjectMocks
    private EventIndexerCatchupBySubscriptionRunner eventIndexerCatchupBySubscriptionRunner;

    @Test
    public void shouldCreateEventCatchupTaskForASubscriptionAndRunInASeparateProcess() throws Exception {

        final String componentName = "EVENT_LISTENER";

        final Subscription subscription = mock(Subscription.class);

        final EventIndexerCatchupTask eventIndexerCatchupTask = new EventIndexerCatchupTask(
                subscription,
                eventIndexerCatchupProcessorBean, componentName);

        eventIndexerCatchupBySubscriptionRunner.runEventIndexerCatchupForSubscription(subscription, componentName);

        verify(managedExecutorService).submit(eventIndexerCatchupTask);
    }
}
