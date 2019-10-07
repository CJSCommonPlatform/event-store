package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventUpdaterTest {

    @Mock
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Mock
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Mock
    private  BatchPublishedEventProcessor batchPublishedEventProcessor;

    @Mock
    private ProcessCompleteDecider processCompleteDecider;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishedEventUpdater publishedEventUpdater;

    @Test
    public void shouldIterateThroughAllBatchesOfEventsAndProcessThem() throws Exception {

        final Set<UUID> activeStreamIds = newHashSet(randomUUID());
        final BatchProcessDetails startBatchProcessDetails = mock(BatchProcessDetails.class);
        final BatchProcessDetails nextBatchProcessDetails = mock(BatchProcessDetails.class);
        final BatchProcessDetails finalBatchProcessDetails = mock(BatchProcessDetails.class);

        when(activeEventStreamIdProvider.getActiveStreamIds()).thenReturn(activeStreamIds);
        when(batchProcessingDetailsCalculator.createFirstBatchProcessDetails()).thenReturn(startBatchProcessDetails);
        when(processCompleteDecider.isProcessingComplete(startBatchProcessDetails)).thenReturn(false);
        when(batchPublishedEventProcessor.processNextBatchOfEvents(startBatchProcessDetails, activeStreamIds)).thenReturn(nextBatchProcessDetails);
        when(processCompleteDecider.isProcessingComplete(nextBatchProcessDetails)).thenReturn(false);
        when(batchPublishedEventProcessor.processNextBatchOfEvents(nextBatchProcessDetails, activeStreamIds)).thenReturn(finalBatchProcessDetails);
        when(processCompleteDecider.isProcessingComplete(finalBatchProcessDetails)).thenReturn(true);
        when(finalBatchProcessDetails.getProcessCount()).thenReturn(23);

        publishedEventUpdater.createPublishedEvents();

        verify(logger).info("Creating PublishedEvents..");
        verify(logger).info("Inserted 23 PublishedEvents in total");
    }
}
