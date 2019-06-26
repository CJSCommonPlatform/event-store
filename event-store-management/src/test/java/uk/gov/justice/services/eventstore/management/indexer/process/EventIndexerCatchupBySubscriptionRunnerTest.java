package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventstore.management.catchup.process.CatchupContext;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupTask;
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

        final IndexerCatchupContext catchupContext = mock(IndexerCatchupContext.class);

        final EventIndexerCatchupTask eventCatchupTask = new EventIndexerCatchupTask(
                catchupContext,
                eventIndexerCatchupProcessorBean);

        eventIndexerCatchupBySubscriptionRunner.runEventIndexerCatchupForSubscription(catchupContext);

        verify(managedExecutorService).submit(eventCatchupTask);
    }
}
