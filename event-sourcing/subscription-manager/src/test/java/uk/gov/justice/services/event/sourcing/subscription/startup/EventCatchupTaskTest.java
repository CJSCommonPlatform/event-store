package uk.gov.justice.services.event.sourcing.subscription.startup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;

public class EventCatchupTaskTest {

    @Test
    public void shouldStartSubscription() throws Exception {

        final String componentName = "component name";

        final Subscription subscription = mock(Subscription.class);
        final EventCatchupProcessorBean eventCatchupProcessorBean = mock(EventCatchupProcessorBean.class);

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(componentName, subscription, eventCatchupProcessorBean);

        eventCatchupTask.run();

        verify(eventCatchupProcessorBean).performEventCatchup(
                componentName,
                subscription
        );
    }
}
