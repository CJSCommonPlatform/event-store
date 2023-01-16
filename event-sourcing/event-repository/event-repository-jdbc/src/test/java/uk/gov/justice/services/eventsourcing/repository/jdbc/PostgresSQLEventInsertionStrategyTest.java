package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.BaseEventInsertStrategy.SQL_INSERT_EVENT;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostgresSQLEventInsertionStrategyTest {

    private static final int INSERTED = 1;
    private static final int CONFLICT_OCCURRED = 0;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final long SEQUENCE_ID = 1L;
    private static final String NAME = "Name";
    private static final String METADATA = "metadata";
    private static final String PAYLOAD = "payload";
    final ZonedDateTime createdAt = new UtcClock().now();

    @Mock
    private Event event;

    @Mock
    private PreparedStatementWrapper preparedStatement;

    @InjectMocks
    private PostgresSQLEventLogInsertionStrategy strategy;

    @Test
    public void shouldExecutePreparedStatementAndCompleteIfRowIsInserted() throws Exception {
        when(event.getId()).thenReturn(ID);
        when(event.getStreamId()).thenReturn(STREAM_ID);
        when(event.getPositionInStream()).thenReturn(SEQUENCE_ID);
        when(event.getName()).thenReturn(NAME);
        when(event.getMetadata()).thenReturn(METADATA);
        when(event.getPayload()).thenReturn(PAYLOAD);
        when(preparedStatement.executeUpdate()).thenReturn(INSERTED);
        when(event.getCreatedAt()).thenReturn(createdAt);

        strategy.insert(preparedStatement, event);

        verify(preparedStatement).setObject(1, ID);
        verify(preparedStatement).setObject(2, STREAM_ID);
        verify(preparedStatement).setLong(3, SEQUENCE_ID);
        verify(preparedStatement).setString(4, NAME);
        verify(preparedStatement).setString(5, METADATA);
        verify(preparedStatement).setString(6, PAYLOAD);
        verify(preparedStatement).setTimestamp(7, toSqlTimestamp(createdAt));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldExecutePreparedStatementAndThrowExceptionIfRowWasNotInsertedDueToConflict() throws Exception {
        when(event.getId()).thenReturn(ID);
        when(event.getStreamId()).thenReturn(STREAM_ID);
        when(event.getPositionInStream()).thenReturn(SEQUENCE_ID);
        when(event.getName()).thenReturn(NAME);
        when(event.getMetadata()).thenReturn(METADATA);
        when(event.getPayload()).thenReturn(PAYLOAD);
        when(preparedStatement.executeUpdate()).thenReturn(CONFLICT_OCCURRED);
        when(event.getCreatedAt()).thenReturn(createdAt);


        final OptimisticLockingRetryException optimisticLockingRetryException = assertThrows(OptimisticLockingRetryException.class, () ->
                strategy.insert(preparedStatement, event)
        );

        assertThat(optimisticLockingRetryException.getMessage(), is("Locking Exception while storing sequence 1 of stream " + STREAM_ID));
        verify(preparedStatement).setObject(1, ID);
        verify(preparedStatement).setObject(2, STREAM_ID);
        verify(preparedStatement).setLong(3, SEQUENCE_ID);
        verify(preparedStatement).setString(4, NAME);
        verify(preparedStatement).setString(5, METADATA);
        verify(preparedStatement).setString(6, PAYLOAD);
        verify(preparedStatement).setTimestamp(7, toSqlTimestamp(createdAt));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void shouldReturnTheDefaultSqlInsertStatementWithPostgresDoNothingSuffix() throws Exception {
        assertThat(strategy.insertStatement(), is(SQL_INSERT_EVENT + " ON CONFLICT DO NOTHING"));
    }
}
