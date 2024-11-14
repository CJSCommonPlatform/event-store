package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class EventBufferJdbcRepositoryTest {
    private static final String SELECT_STREAM_BUFFER_BY_STREAM_ID_SOURCE_AND_COMPONENT = "SELECT stream_id, position, event, source, component, buffered_at FROM stream_buffer WHERE stream_id=? AND source=? AND component=? ORDER BY position";
    private static final String INSERT = "INSERT INTO stream_buffer (stream_id, position, event, source, component, buffered_at) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
    private static final String DELETE_BY_STREAM_ID_POSITION = "DELETE FROM stream_buffer WHERE stream_id=? AND position=? AND source=? AND component=?";

    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private PreparedStatementWrapper preparedStatementWrapper;

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventBufferJdbcRepository eventBufferJdbcRepository;

    public static final String EVENT_LISTENER = "event_listener";

    @BeforeEach
    public void initDatabase() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void shouldInsertEvent() throws SQLException {

        final UUID streamId = randomUUID();
        final long position = 1l;
        final String source = "source";
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER, bufferedAt));

        verify(preparedStatement).setObject(1, streamId);
        verify(preparedStatement).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);
        verify(preparedStatement).executeUpdate();
        verifyNoInteractions(logger);
    }

    @Test
    public void shouldWarnIfInsertDoesNothing() throws SQLException {

        final UUID streamId = randomUUID();
        final long position = 1l;
        final String source = "source";
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER, bufferedAt));

        verify(preparedStatement).setObject(1, streamId);
        verify(preparedStatement).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);
        verify(preparedStatement).executeUpdate();
        verify(logger).warn("Event already present in event buffer. Ignoring");
    }

    @Test
    public void shouldThrowExceptionWhileStoringEvent() throws SQLException {
        final String source = "source";
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT))
                .thenThrow(new SQLException());

        final UUID streamId = randomUUID();
        final long position = 1l;
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER, bufferedAt);

        assertThrows(JdbcRepositoryException.class, () -> eventBufferJdbcRepository.insert(eventBufferEvent));
    }

    @Test
    public void shouldReturnBufferedEvent() throws SQLException {

        final String source = "source";
        final String component = EVENT_LISTENER;
        final ZonedDateTime bufferedAt = new UtcClock().now();

        final Function<ResultSet, EventBufferEvent> function = mock(Function.class);
        final Stream<EventBufferEvent> stream = mock(Stream.class);

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(SELECT_STREAM_BUFFER_BY_STREAM_ID_SOURCE_AND_COMPONENT))
                .thenReturn(preparedStatement);

        final UUID streamId = randomUUID();
        final long position = 1L;
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, component, bufferedAt));

        eventBufferJdbcRepository.findStreamByIdSourceAndComponent(streamId, source, component);

        verify(preparedStatement, times(2)).setObject(1, streamId);
        verify(preparedStatement).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, component);
        verify(preparedStatement).setString(2, source);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldThrowExceptionWhileReturningBufferedEvent() throws SQLException {

        final UUID streamId = randomUUID();
        final long position = 1l;
        final String source = "source";
        final String component = EVENT_LISTENER;
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(SELECT_STREAM_BUFFER_BY_STREAM_ID_SOURCE_AND_COMPONENT))
                .thenThrow(new SQLException());

        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, component, bufferedAt));

        assertThrows(JdbcRepositoryException.class, () -> eventBufferJdbcRepository.findStreamByIdSourceAndComponent(streamId, source, component));
   }

    @Test
    public void shouldRemoveBufferedEvent() throws SQLException {
        final String source = "source";
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(DELETE_BY_STREAM_ID_POSITION))
                .thenReturn(preparedStatement);

        final UUID streamId = randomUUID();
        final long position = 1l;
        EventBufferEvent eventBufferEvent = new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER, bufferedAt);
        eventBufferJdbcRepository.insert(eventBufferEvent);

        eventBufferJdbcRepository.remove(eventBufferEvent);

        verify(preparedStatement, times(2)).setObject(1, streamId);
        verify(preparedStatement, times(2)).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);

        verify(preparedStatement).setString(3, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);
        verify(preparedStatement, times(2)).executeUpdate();
    }

    @Test
    public void shouldThrowExceptionWhileRemovingEventFromBuffer() throws SQLException {

        final String source = "source";
        final ZonedDateTime bufferedAt = new UtcClock().now();

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(DELETE_BY_STREAM_ID_POSITION))
                .thenThrow(new SQLException());

        final UUID streamId = randomUUID();
        final long position = 1l;
        EventBufferEvent eventBufferEvent = new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER, bufferedAt);

        eventBufferJdbcRepository.insert(eventBufferEvent);
        assertThrows(JdbcRepositoryException.class, () -> eventBufferJdbcRepository.remove(eventBufferEvent));
    }
}
