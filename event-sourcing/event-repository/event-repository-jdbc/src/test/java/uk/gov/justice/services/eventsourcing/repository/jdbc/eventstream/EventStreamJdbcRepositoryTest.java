package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamJdbcRepositoryTest {

    private static final String SQL_FIND_EVENT_STREAM = "SELECT * FROM event_stream s WHERE s.stream_id=?";
    private static final String SQL_INSERT_EVENT_STREAM = "INSERT INTO event_stream (stream_id, date_created, active) values (?, ?, ?) ON CONFLICT DO NOTHING";

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Mock
    private UtcClock clock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PreparedStatementWrapper queryPreparedStatementWrapper, insertPreparedStatementWrapper;

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @InjectMocks
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Test
    public void insertActiveStreamSuccessfully() throws SQLException {

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(false);

        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM)).thenReturn(insertPreparedStatementWrapper);
        final ZonedDateTime streamCreationTimestamp = ZonedDateTime.now();
        when(clock.now()).thenReturn(streamCreationTimestamp);
        when(insertPreparedStatementWrapper.executeUpdate()).thenReturn(1);

        final UUID streamId = randomUUID();
        eventStreamJdbcRepository.insert(streamId);

        verify(preparedStatementWrapperFactory).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
        verify(queryPreparedStatementWrapper).setObject(1, streamId);
        verify(preparedStatementWrapperFactory).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
        verify(insertPreparedStatementWrapper).setObject(1, streamId);
        verify(insertPreparedStatementWrapper).setTimestamp(2, ZonedDateTimes.toSqlTimestamp(streamCreationTimestamp));
        verify(insertPreparedStatementWrapper).setBoolean(3, true);
        verify(insertPreparedStatementWrapper).executeUpdate();
    }

    @Test
    public void insertExistingStreamAndRecordOptimisticLockingException() throws SQLException {

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        // indicates stream doesn't exist
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(false);

        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM)).thenReturn(insertPreparedStatementWrapper);
        final ZonedDateTime streamCreationTimestamp = ZonedDateTime.now();
        when(clock.now()).thenReturn(streamCreationTimestamp);
        // not able to insert stream as another transaction inserted a stream with the same identifier after the check above
        when(insertPreparedStatementWrapper.executeUpdate()).thenReturn(0);

        final UUID streamId = randomUUID();
        try {
            eventStreamJdbcRepository.insert(streamId);
            fail("Exception should be thrown when 0 records are updated");
        } catch (OptimisticLockingRetryException e) {
            verify(preparedStatementWrapperFactory).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
            verify(queryPreparedStatementWrapper).setObject(1, streamId);
            verify(preparedStatementWrapperFactory).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
            verify(insertPreparedStatementWrapper).setObject(1, streamId);
            verify(insertPreparedStatementWrapper).setTimestamp(2, ZonedDateTimes.toSqlTimestamp(streamCreationTimestamp));
            verify(insertPreparedStatementWrapper).setBoolean(3, true);
            verify(insertPreparedStatementWrapper).executeUpdate();
        }
    }

    @Test
    public void insertExistingStreamAndReturnWithoutException() throws SQLException {

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        // indicates stream already exists
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(true);

        final UUID streamId = randomUUID();
        eventStreamJdbcRepository.insert(streamId);
        verify(preparedStatementWrapperFactory).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
        verify(queryPreparedStatementWrapper).setObject(1, streamId);
        verify(preparedStatementWrapperFactory, never()).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
        verify(insertPreparedStatementWrapper, never()).setObject(1, streamId);
        verify(insertPreparedStatementWrapper, never()).setTimestamp(eq(2), any());
        verify(insertPreparedStatementWrapper, never()).setBoolean(3, true);
        verify(insertPreparedStatementWrapper, never()).executeUpdate();
    }
}
