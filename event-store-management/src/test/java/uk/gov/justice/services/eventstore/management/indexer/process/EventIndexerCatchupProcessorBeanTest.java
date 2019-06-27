package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventIndexerCatchupProcessorBeanTest {

    @Mock
    private EventIndexerCatchupProcessorFactory eventCatchupProcessorFactory;

    @InjectMocks
    private EventIndexerCatchupProcessorBean eventCatchupProcessorBean;

    @Test
    public void shouldPerformEventCatchup() throws Exception {

        final String componentName = "EVENT_INDEXER";
        final IndexerCatchupContext catchupContext = mock(IndexerCatchupContext.class);

        final Subscription subscription = mock(Subscription.class);
        final EventIndexerCatchupProcessor eventCatchupProcessor = mock(EventIndexerCatchupProcessor.class);

        when(eventCatchupProcessorFactory.create()).thenReturn(eventCatchupProcessor);

        eventCatchupProcessorBean.performEventIndexerCatchup(catchupContext);

        verify(eventCatchupProcessor).performEventIndexerCatchup(catchupContext);
    }
}
