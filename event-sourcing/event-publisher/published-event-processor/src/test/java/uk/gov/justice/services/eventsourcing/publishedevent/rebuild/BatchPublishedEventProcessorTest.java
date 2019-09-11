package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class BatchPublishedEventProcessorTest {

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private PublishedEventInserter publishedEventInserter;

    @Mock
    private BatchProcessingDetailsCalculator batchProcessingDetailsCalculator;

    @Mock
    private Logger logger;

    @InjectMocks
    private BatchPublishedEventProcessor batchPublishedEventProcessor;

    @Test
    public void shouldGetAndProcessTheNextBatchOfEvents() throws Exception {

        final AtomicLong previousEventNumber = new AtomicLong(22L);
        final AtomicLong currentEventNumber = new AtomicLong(23L);

        final BatchProcessDetails currentBatchProcessDetails = mock(BatchProcessDetails.class);
        final BatchProcessDetails nextBatchProcessDetails = mock(BatchProcessDetails.class);
        final Set<UUID> activeStreamIds = newHashSet(randomUUID());

        final Event event_23 = mock(Event.class);
        final Event event_24 = mock(Event.class);
        final Event event_25 = mock(Event.class);

        final PublishedEvent publishedEvent_23 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_24 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_25 = mock(PublishedEvent.class);
        final List<PublishedEvent> publishedEvents = asList(publishedEvent_23, publishedEvent_24, publishedEvent_25);

        when(currentBatchProcessDetails.getCurrentEventNumber()).thenReturn(currentEventNumber);
        when(currentBatchProcessDetails.getPreviousEventNumber()).thenReturn(previousEventNumber);
        when(eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), 1_000)).thenReturn(Stream.of(event_23, event_24, event_25));

        when(publishedEventInserter.convertAndSave(event_23, previousEventNumber, activeStreamIds)).thenReturn(of(publishedEvent_23));
        when(publishedEventInserter.convertAndSave(event_24, previousEventNumber, activeStreamIds)).thenReturn(of(publishedEvent_24));
        when(publishedEventInserter.convertAndSave(event_25, previousEventNumber, activeStreamIds)).thenReturn(of(publishedEvent_25));
        when(nextBatchProcessDetails.getProcessCount()).thenReturn(publishedEvents.size());

        when(batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                currentBatchProcessDetails,
                previousEventNumber,
                publishedEvents)).thenReturn(nextBatchProcessDetails);

        assertThat(batchPublishedEventProcessor.processNextBatchOfEvents(currentBatchProcessDetails, activeStreamIds), is(nextBatchProcessDetails));

        verify(logger).info("Inserted 3 PublishedEvents");
    }

    @Test
    public void shouldIgnoreMissingPublisedEvents() throws Exception {

        final AtomicLong previousEventNumber = new AtomicLong(22L);
        final AtomicLong currentEventNumber = new AtomicLong(23L);

        final BatchProcessDetails currentBatchProcessDetails = mock(BatchProcessDetails.class);
        final BatchProcessDetails nextBatchProcessDetails = mock(BatchProcessDetails.class);
        final Set<UUID> activeStreamIds = newHashSet(randomUUID());

        final Event event_23 = mock(Event.class);
        final Event event_24 = mock(Event.class);
        final Event event_25 = mock(Event.class);

        final PublishedEvent publishedEvent_23 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_25 = mock(PublishedEvent.class);
        final List<PublishedEvent> publishedEvents = asList(publishedEvent_23, publishedEvent_25);

        when(currentBatchProcessDetails.getCurrentEventNumber()).thenReturn(currentEventNumber);
        when(currentBatchProcessDetails.getPreviousEventNumber()).thenReturn(previousEventNumber);
        when(eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), 1_000)).thenReturn(Stream.of(event_23, event_24, event_25));

        when(publishedEventInserter.convertAndSave(event_23, previousEventNumber, activeStreamIds)).thenReturn(of(publishedEvent_23));
        when(publishedEventInserter.convertAndSave(event_24, previousEventNumber, activeStreamIds)).thenReturn(empty());
        when(publishedEventInserter.convertAndSave(event_25, previousEventNumber, activeStreamIds)).thenReturn(of(publishedEvent_25));
        when(nextBatchProcessDetails.getProcessCount()).thenReturn(publishedEvents.size());

        assertThat(nextBatchProcessDetails.getProcessCount(), is(2));

        when(batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                currentBatchProcessDetails,
                previousEventNumber,
                publishedEvents)).thenReturn(nextBatchProcessDetails);

        assertThat(batchPublishedEventProcessor.processNextBatchOfEvents(currentBatchProcessDetails, activeStreamIds), is(nextBatchProcessDetails));

        verify(logger).info("Inserted 2 PublishedEvents");
    }
}
