package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessCompleteDeciderTest {

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @InjectMocks
    private ProcessCompleteDecider processCompleteDecider;

    @Test
    public void shouldReturnFalseIfMoreEventsToProcess() {

        final BatchProcessDetails batchProcessDetails = mock(BatchProcessDetails.class);

        when(batchProcessDetails.getCurrentEventNumber()).thenReturn(new AtomicLong(23L));
        when(eventJdbcRepository.getMaximumEventNumber()).thenReturn(24L);

        final boolean processingComplete = processCompleteDecider.isProcessingComplete(batchProcessDetails);

        assertThat(processingComplete, is(false));
    }

    @Test
    public void shouldReturnTrueIfNoMoreEventsToProcess() {

        final BatchProcessDetails batchProcessDetails = mock(BatchProcessDetails.class);

        when(batchProcessDetails.getCurrentEventNumber()).thenReturn(new AtomicLong(23L));
        when(eventJdbcRepository.getMaximumEventNumber()).thenReturn(23L);

        final boolean processingComplete = processCompleteDecider.isProcessingComplete(batchProcessDetails);

        assertThat(processingComplete, is(true));
    }

    @Test
    public void shouldReturnTrueIfLessThanNoMoreEventsToProcess() {

        final BatchProcessDetails batchProcessDetails = mock(BatchProcessDetails.class);

        when(batchProcessDetails.getCurrentEventNumber()).thenReturn(new AtomicLong(23L));
        when(eventJdbcRepository.getMaximumEventNumber()).thenReturn(22L);

        final boolean processingComplete = processCompleteDecider.isProcessingComplete(batchProcessDetails);

        assertThat(processingComplete, is(true));
    }
}
