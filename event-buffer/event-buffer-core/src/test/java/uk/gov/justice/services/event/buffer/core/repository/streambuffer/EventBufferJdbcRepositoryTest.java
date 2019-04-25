package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventBufferJdbcRepositoryTest {
    private static final String SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE = "SELECT stream_id, position, event, source, component FROM stream_buffer WHERE stream_id=? AND source=? ORDER BY position";
    private static final String INSERT = "INSERT INTO stream_buffer (stream_id, position, event, source, component) VALUES (?, ?, ?, ?, ?)";
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

    @InjectMocks
    private EventBufferJdbcRepository eventBufferJdbcRepository;

    public static final String EVENT_LISTENER = "event_listener";

    @Before
    public void initDatabase() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    @Test
    public void shouldInsertEvent() throws SQLException {
        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        final UUID streamId = randomUUID();
        final long position = 1l;
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER));

        verify(preparedStatement).setObject(1, streamId);
        verify(preparedStatement).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);
        verify(preparedStatement).executeUpdate();
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionWhileStoringEvent() throws SQLException {
        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenThrow(new SQLException());

        final UUID streamId = randomUUID();
        final long position = 1l;
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER));
    }

    @Test
    public void shouldReturnBufferedEvent() throws SQLException {
        Function<ResultSet, EventBufferEvent> function = mock(Function.class);

        Stream<EventBufferEvent> stream = mock(Stream.class);

        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE))
                .thenReturn(preparedStatement);

        when(jdbcResultSetStreamer.streamOf(preparedStatementWrapper, function))
                .thenReturn(stream);

        final UUID streamId = randomUUID();
        final long position = 1l;
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER));

        eventBufferJdbcRepository.findStreamByIdAndSource(streamId, source);

        verify(preparedStatement, times(2)).setObject(1, streamId);
        verify(preparedStatement).setLong(2, position);
        verify(preparedStatement).setString(3, "eventVersion_2");
        verify(preparedStatement).setString(4, source);
        verify(preparedStatement).setString(5, EVENT_LISTENER);
        verify(preparedStatement).setString(2, source);
        verify(preparedStatement).executeUpdate();
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionWhileReturningBufferedEvent() throws SQLException {

        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(SELECT_STREAM_BUFFER_BY_STREAM_ID_AND_SOURCE))
                .thenThrow(new SQLException());

        final UUID streamId = randomUUID();
        final long position = 1l;
        eventBufferJdbcRepository.insert(new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER));
        eventBufferJdbcRepository.findStreamByIdAndSource(streamId, source);
   }

    @Test
    public void shouldRemoveBufferedEvent() throws SQLException {
        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(DELETE_BY_STREAM_ID_POSITION))
                .thenReturn(preparedStatement);

        final UUID streamId = randomUUID();
        final long position = 1l;
        EventBufferEvent eventBufferEvent = new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER);
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

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionWhileRemovingEventFromBuffer() throws SQLException {

        final String source = "source";

        when(connection.prepareStatement(INSERT))
                .thenReturn(preparedStatement);

        when(connection.prepareStatement(DELETE_BY_STREAM_ID_POSITION))
                .thenThrow(new SQLException());

        final UUID streamId = randomUUID();
        final long position = 1l;
        EventBufferEvent eventBufferEvent = new EventBufferEvent(streamId, position, "eventVersion_2", source, EVENT_LISTENER);

        eventBufferJdbcRepository.insert(eventBufferEvent);
        eventBufferJdbcRepository.remove(eventBufferEvent);
    }

}