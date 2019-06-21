package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EventIndexerCatchupTaskTest {

    @Test
    public void shouldStartSubscription() throws Exception {

        final IndexerCatchupContext indexerCatchupContext = mock(IndexerCatchupContext.class);
        final EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean = mock(EventIndexerCatchupProcessorBean.class);

        final EventIndexerCatchupTask eventCatchupTask = new EventIndexerCatchupTask(indexerCatchupContext, eventIndexerCatchupProcessorBean);

        assertThat(eventCatchupTask.call(), is(true));

        verify(eventIndexerCatchupProcessorBean).performEventIndexerCatchup(indexerCatchupContext);
    }
}
