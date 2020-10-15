package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BatchProcessingDetailsCalculatorTest {

    @InjectMocks
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Test
    public void shouldStartWithProcessingDetailsOfZero() throws Exception {

        final BatchProcessDetails batchProcessDetails = batchProcessingDetailsCalculator.createFirstBatchProcessDetails();

        assertThat(batchProcessDetails.getCurrentEventNumber().get(), is(0L));
        assertThat(batchProcessDetails.getPreviousEventNumber().get(), is(0L));
        assertThat(batchProcessDetails.getProcessCount(), is(0));
    }

    @Test
    public void shouldCalculateTheNextProcessingDetailsBasedOnTheCurrentProcessedEvents() throws Exception {

        final int processedCount = 20;

        final BatchProcessDetails previousProcessDetails = new BatchProcessDetails(
                new AtomicLong(19),
                new AtomicLong(20),
                processedCount,
                20
        );

        final PublishedEvent event_21 = mock(PublishedEvent.class);
        final PublishedEvent event_22 = mock(PublishedEvent.class);
        final PublishedEvent event_23 = mock(PublishedEvent.class);

        final List<PublishedEvent> publishedEvents = asList(event_21, event_22, event_23);

        final AtomicLong previousEventNumber = new AtomicLong(22L);
        final AtomicLong currentEventNumber = new AtomicLong(23L);

        final BatchProcessDetails nextBatchProcessDetails = batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                previousProcessDetails,
                currentEventNumber,
                previousEventNumber,
                publishedEvents);

        assertThat(nextBatchProcessDetails.getProcessedInBatchCount(), is(3));
        assertThat(nextBatchProcessDetails.getProcessCount(), is(23));
        assertThat(nextBatchProcessDetails.getPreviousEventNumber(), is(previousEventNumber));
        assertThat(nextBatchProcessDetails.getCurrentEventNumber().get(), is(23L));
    }
}
