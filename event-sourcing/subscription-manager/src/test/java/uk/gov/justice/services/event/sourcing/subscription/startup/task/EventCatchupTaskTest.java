package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.event.sourcing.subscription.startup.EventCatchupProcessorBean;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;

public class EventCatchupTaskTest {

    @Test
    public void shouldStartSubscription() throws Exception {

        final String componentName = "component name";

        final Subscription subscription = mock(Subscription.class);
        final EventCatchupProcessorBean eventCatchupProcessorBean = mock(EventCatchupProcessorBean.class);

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(componentName, subscription, eventCatchupProcessorBean);

        assertThat(eventCatchupTask.call(), is(true));

        verify(eventCatchupProcessorBean).performEventCatchup(
                componentName,
                subscription
        );
    }
}
