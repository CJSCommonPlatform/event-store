package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventsProcessorTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private PublishedEventProcessor publishedEventProcessor;

    @Mock
    private PublishedEventInserter publishedEventInserter;

    @InjectMocks
    private PublishedEventsProcessor publishedEventsProcessor;

    @Test
    public void shouldCreatePublishedEvents() throws SQLException {
        final Connection connection = mock(Connection.class);
        when(eventStoreDataSourceProvider.getDefaultDataSource().getConnection()).thenReturn(connection);

        final Event event1 = mock(Event.class);
        final Event event2 = mock(Event.class);
        final Event event3 = mock(Event.class);

        final UUID streamID1 = randomUUID();
        final Stream<Event> eventIds = Stream.of(event1, event2, event3);

        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamID1)).thenReturn(eventIds);

        publishedEventsProcessor.populatePublishedEvents(streamID1, eventJdbcRepository);

        verify(publishedEventProcessor).createPublishedEvent(event1);
        verify(publishedEventProcessor).createPublishedEvent(event2);
        verify(publishedEventProcessor).createPublishedEvent(event3);
    }

    @Test
    public void shouldTruncateEvents() throws Exception {

        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource().getConnection()).thenReturn(connection);

        publishedEventsProcessor.truncatePublishedEvents();

        verify(publishedEventInserter).truncate(connection);
    }

    @Test
    public void shouldThrowPublishedEventSQLExceptionOnFailure() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource().getConnection()).thenReturn(connection);
        doThrow(sqlException).when(publishedEventInserter).truncate(connection);

        try {
            publishedEventsProcessor.truncatePublishedEvents();
            fail();
        } catch (final PublishedEventSQLException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to truncate Linked Events table"));
        }
    }
}
