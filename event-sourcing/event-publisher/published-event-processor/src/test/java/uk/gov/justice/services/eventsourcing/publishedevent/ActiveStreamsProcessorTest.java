package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.time.ZonedDateTime.now;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.DatasourceException;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStreamsProcessorTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private PublishedEventsProcessor publishedEventsProcessors;

    @InjectMocks
    private ActiveStreamsProcessor activeStreamsProcessor;

    @Test
    public void shouldProcessActiveStreams() throws SQLException, DatasourceException {
        final UUID streamId1 = UUID.randomUUID();
        final UUID streamId2 = UUID.randomUUID();
        final UUID streamId3 = UUID.randomUUID();

        final EventStream eventStream1 = new EventStream(streamId1, 1L, true, now());
        final EventStream eventStream2 = new EventStream(streamId2, 1L, true, now());
        final EventStream eventStream3 = new EventStream(streamId3, 1L, true, now());

        when(defaultEventSourceDefinitionFactory.createDefaultEventSource().getLocation().getDataSource()).thenReturn(java.util.Optional.of(""));

        when(eventJdbcRepositoryFactory.eventJdbcRepository("")).thenReturn(eventJdbcRepository);
        when(eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository("")).thenReturn(eventStreamJdbcRepository);

        final Stream<EventStream> eventStreams = Stream.of(eventStream1, eventStream2, eventStream3);

        when(eventStreamJdbcRepository.findActive()).thenReturn(eventStreams);

        activeStreamsProcessor.populatePublishedEvents();

        verify(publishedEventsProcessors).populatePublishedEvents(streamId1, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId2, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId3, eventJdbcRepository);
    }
}
