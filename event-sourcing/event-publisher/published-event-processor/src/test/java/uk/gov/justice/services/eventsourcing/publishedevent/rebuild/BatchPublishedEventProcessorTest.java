package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    private PublishedEventsRebuilder publishedEventsRebuilder;

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
        final Stream<Event> eventStream = Stream.of(event_23, event_24, event_25);

        final PublishedEvent publishedEvent_23 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_24 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_25 = mock(PublishedEvent.class);
        final List<PublishedEvent> publishedEvents = asList(publishedEvent_23, publishedEvent_24, publishedEvent_25);

        when(currentBatchProcessDetails.getCurrentEventNumber()).thenReturn(currentEventNumber);
        when(currentBatchProcessDetails.getPreviousEventNumber()).thenReturn(previousEventNumber);
        when(eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), 1_000)).thenReturn(eventStream);

        when(publishedEventsRebuilder.rebuild(
                eventStream,
                previousEventNumber, currentEventNumber,
                activeStreamIds)).thenReturn(publishedEvents);

        when(batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                currentBatchProcessDetails,
                currentEventNumber,
                previousEventNumber,
                publishedEvents)).thenReturn(nextBatchProcessDetails);

        when(nextBatchProcessDetails.getProcessedInBatchCount()).thenReturn(3);
        when(nextBatchProcessDetails.getProcessCount()).thenReturn(3);

        assertThat(batchPublishedEventProcessor.processNextBatchOfEvents(currentBatchProcessDetails, activeStreamIds), is(nextBatchProcessDetails));

        verify(logger).info("Inserted 3 PublishedEvents");
    }

    @Test
    public void shouldNotLogIfNoEventsProcessedInThisBatch() throws Exception {

        final AtomicLong previousEventNumber = new AtomicLong(22L);
        final AtomicLong currentEventNumber = new AtomicLong(23L);

        final BatchProcessDetails currentBatchProcessDetails = mock(BatchProcessDetails.class);
        final BatchProcessDetails nextBatchProcessDetails = mock(BatchProcessDetails.class);
        final Set<UUID> activeStreamIds = newHashSet(randomUUID());

        final Event event_23 = mock(Event.class);
        final Event event_24 = mock(Event.class);
        final Event event_25 = mock(Event.class);
        final Stream<Event> eventStream = Stream.of(event_23, event_24, event_25);

        final PublishedEvent publishedEvent_23 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_24 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_25 = mock(PublishedEvent.class);
        final List<PublishedEvent> publishedEvents = asList(publishedEvent_23, publishedEvent_24, publishedEvent_25);

        when(currentBatchProcessDetails.getCurrentEventNumber()).thenReturn(currentEventNumber);
        when(currentBatchProcessDetails.getPreviousEventNumber()).thenReturn(previousEventNumber);
        when(eventJdbcRepository.findAllFromEventNumberUptoPageSize(currentEventNumber.get(), 1_000)).thenReturn(eventStream);

        when(publishedEventsRebuilder.rebuild(
                eventStream,
                previousEventNumber, currentEventNumber,
                activeStreamIds)).thenReturn(publishedEvents);

        when(batchProcessingDetailsCalculator.calculateNextBatchProcessDetails(
                currentBatchProcessDetails,
                currentEventNumber,
                previousEventNumber,
                publishedEvents)).thenReturn(nextBatchProcessDetails);

        when(nextBatchProcessDetails.getProcessedInBatchCount()).thenReturn(0);

        assertThat(batchPublishedEventProcessor.processNextBatchOfEvents(currentBatchProcessDetails, activeStreamIds), is(nextBatchProcessDetails));

        verify(logger).info("Skipping inactive events...");
    }
}
