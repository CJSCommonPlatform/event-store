package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;

public class EventIndexerCatchupTaskTest {

    @Test
    public void shouldStartSubscription() throws Exception {

        final String componentName = "EVENT_INDEXER";

        final Subscription subscription = mock(Subscription.class);
        final EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean = mock(EventIndexerCatchupProcessorBean.class);

        final EventIndexerCatchupTask eventIndexerCatchupTask = new EventIndexerCatchupTask(subscription, eventIndexerCatchupProcessorBean, componentName);

        assertThat(eventIndexerCatchupTask.call(), is(true));

        verify(eventIndexerCatchupProcessorBean).performEventIndexerCatchup(subscription, componentName);
    }
}
