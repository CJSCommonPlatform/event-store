package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamJdbcRepositoryTest {

    private static final String SQL_FIND_EVENT_STREAM = "SELECT * FROM event_stream s WHERE s.stream_id=?";
    private static final String SQL_INSERT_EVENT_STREAM = "INSERT INTO event_stream (stream_id, date_created, active) values (?, ?, ?) ON CONFLICT DO NOTHING";

    @Mock
    private Logger logger;

    @Mock
    private JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Mock
    private UtcClock clock;

    private final String jndiDatasource = "jndiDatasource";

    @Mock
    private DataSource dataSource;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PreparedStatementWrapper queryPreparedStatementWrapper, insertPreparedStatementWrapper;

    @InjectMocks
    private EventStreamJdbcRepository repository;

    @Before
    public void setup() {
        setField(repository, "jndiDatasource", jndiDatasource);
        when(jdbcDataSourceProvider.getDataSource(jndiDatasource)).thenReturn(dataSource);
    }

    @Test
    public void insertActiveStreamSuccessfully() throws SQLException {

        when(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(false);

        when(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM)).thenReturn(insertPreparedStatementWrapper);
        final ZonedDateTime streamCreationTimestamp = ZonedDateTime.now();
        when(clock.now()).thenReturn(streamCreationTimestamp);
        when(insertPreparedStatementWrapper.executeUpdate()).thenReturn(1);

        final UUID streamId = randomUUID();
        repository.insert(streamId);

        verify(jdbcDataSourceProvider).getDataSource(jndiDatasource);
        verify(eventStreamJdbcRepositoryHelper).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
        verify(queryPreparedStatementWrapper).setObject(1, streamId);
        verify(eventStreamJdbcRepositoryHelper).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
        verify(insertPreparedStatementWrapper).setObject(1, streamId);
        verify(insertPreparedStatementWrapper).setTimestamp(2, ZonedDateTimes.toSqlTimestamp(streamCreationTimestamp));
        verify(insertPreparedStatementWrapper).setBoolean(3, true);
        verify(insertPreparedStatementWrapper).executeUpdate();
    }

    @Test
    public void insertExistingStreamAndRecordOptimisticLockingException() throws SQLException {

        when(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        // indicates stream doesn't exist
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(false);

        when(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM)).thenReturn(insertPreparedStatementWrapper);
        final ZonedDateTime streamCreationTimestamp = ZonedDateTime.now();
        when(clock.now()).thenReturn(streamCreationTimestamp);
        // not able to insert stream as another transaction inserted a stream with the same identifier after the check above
        when(insertPreparedStatementWrapper.executeUpdate()).thenReturn(0);

        final UUID streamId = randomUUID();
        try {
            repository.insert(streamId);
            fail("Exception should be thrown when 0 records are updated");
        } catch (OptimisticLockingRetryException e) {
            verify(jdbcDataSourceProvider).getDataSource(jndiDatasource);
            verify(eventStreamJdbcRepositoryHelper).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
            verify(queryPreparedStatementWrapper).setObject(1, streamId);
            verify(eventStreamJdbcRepositoryHelper).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
            verify(insertPreparedStatementWrapper).setObject(1, streamId);
            verify(insertPreparedStatementWrapper).setTimestamp(2, ZonedDateTimes.toSqlTimestamp(streamCreationTimestamp));
            verify(insertPreparedStatementWrapper).setBoolean(3, true);
            verify(insertPreparedStatementWrapper).executeUpdate();
        }
    }

    @Test
    public void insertExistingStreamAndReturnWithoutException() throws SQLException {

        when(eventStreamJdbcRepositoryHelper.preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM)).thenReturn(queryPreparedStatementWrapper);
        // indicates stream already exists
        when(queryPreparedStatementWrapper.executeQuery().next()).thenReturn(true);


        final UUID streamId = randomUUID();
        repository.insert(streamId);
        verify(jdbcDataSourceProvider).getDataSource(jndiDatasource);
        verify(eventStreamJdbcRepositoryHelper).preparedStatementWrapperOf(dataSource, SQL_FIND_EVENT_STREAM);
        verify(queryPreparedStatementWrapper).setObject(1, streamId);
        verify(eventStreamJdbcRepositoryHelper, never()).preparedStatementWrapperOf(dataSource, SQL_INSERT_EVENT_STREAM);
        verify(insertPreparedStatementWrapper, never()).setObject(1, streamId);
        verify(insertPreparedStatementWrapper, never()).setTimestamp(eq(2), any());
        verify(insertPreparedStatementWrapper, never()).setBoolean(3, true);
        verify(insertPreparedStatementWrapper, never()).executeUpdate();
    }
}