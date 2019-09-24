package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventIndexerCatchupProcessorBeanTest {

    @Mock
    private EventIndexerCatchupProcessor eventCatchupProcessor;

    @InjectMocks
    private EventIndexerCatchupProcessorBean eventCatchupProcessorBean;

    @Test
    public void shouldPerformEventCatchup() throws Exception {

        final IndexerCatchupContext catchupContext = mock(IndexerCatchupContext.class);

        eventCatchupProcessorBean.performEventIndexerCatchup(catchupContext);

        verify(eventCatchupProcessor).performEventIndexerCatchup(catchupContext);
    }
}
