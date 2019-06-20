package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupBySubscriptionRunnerTest {

    @Mock
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Mock
    private ManagedExecutorService managedExecutorService;

    @InjectMocks
    private EventCatchupBySubscriptionRunner eventCatchupBySubscriptionRunner;

    @Test
    public void shouldCreateEventCatchupTaskForASubscriptionAndRunInASeparateProcess() throws Exception {

        final CatchupContext catchupContext = mock(CatchupContext.class);

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                catchupContext,
                eventCatchupProcessorBean);

        eventCatchupBySubscriptionRunner.runEventCatchupForSubscription(catchupContext);

        verify(managedExecutorService).submit(eventCatchupTask);
    }
}
