package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventCatchupProcessorBean;
import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventCatchupTask;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

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

        final String componentName = "EVENT_LISTENER";

        final Subscription subscription = mock(Subscription.class);

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                subscription,
                eventCatchupProcessorBean, componentName);

        eventCatchupBySubscriptionRunner.runEventCatchupForSubscription(subscription, componentName);

        verify(managedExecutorService).submit(eventCatchupTask);
    }
}
