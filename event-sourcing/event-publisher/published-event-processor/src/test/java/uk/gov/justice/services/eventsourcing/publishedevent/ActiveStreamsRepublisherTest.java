package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventsProcessor;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.DatasourceException;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStreamsRepublisherTest {

    @Mock
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Mock
    private PublishedEventsProcessor publishedEventsProcessors;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @InjectMocks
    private ActiveStreamsRepublisher activeStreamsRepublisher;

    @Test
    public void shouldProcessActiveStreams() throws SQLException, DatasourceException {

        final boolean[] onClosedCalled = {false};

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        final Stream<EventStream> eventStreams = Stream.of(
                new EventStream(streamId1, 1L, true, now()),
                new EventStream(streamId2, 1L, true, now()),
                new EventStream(streamId3, 1L, true, now())).onClose(() -> onClosedCalled[0] = true );

        when(eventStreamJdbcRepository.findActive()).thenReturn(eventStreams);

        activeStreamsRepublisher.populatePublishedEvents();

        verify(publishedEventsProcessors).populatePublishedEvents(streamId1, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId2, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId3, eventJdbcRepository);

        assertThat(onClosedCalled[0], is(true));
    }
}
