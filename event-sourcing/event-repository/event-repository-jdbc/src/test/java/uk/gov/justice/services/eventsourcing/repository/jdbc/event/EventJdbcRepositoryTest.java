package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID_AND_POSITION;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID_AND_POSITION_BY_PAGE;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventJdbcRepositoryTest {

    @Mock
    private EventInsertionStrategy eventInsertionStrategy;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Mock
    private DataSource dataSource;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventJdbcRepository eventJdbcRepository;


    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInInsert() throws Exception {
        final UUID streamId = randomUUID();
        final SQLException sqlException = new SQLException();
        final String statement = "STATEMENT";
        final Event event = mock(Event.class);

        when(eventInsertionStrategy.insertStatement()).thenReturn(statement);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, statement)).thenThrow(sqlException);
        when(event.getSequenceId()).thenReturn(5L);
        when(event.getStreamId()).thenReturn(streamId);

        try {
            eventJdbcRepository.insert(event);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Exception while storing sequence 5 of stream " + streamId));
            verify(logger).error("Error persisting event to the database", sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindByStreamIdOrderByPositionAsc() throws Exception {
        final UUID streamId = randomUUID();
        final SQLException sqlException = new SQLException();

        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_BY_STREAM_ID)).thenThrow(sqlException);

        try {
            eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Exception while reading stream " + streamId));
            verify(logger).warn("Failed to read stream {}", streamId, sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindByStreamIdFromPositionOrderByPositionAsc() throws Exception {
        final UUID streamId = randomUUID();
        final long position = 2L;
        final SQLException sqlException = new SQLException();

        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_BY_STREAM_ID_AND_POSITION)).thenThrow(sqlException);

        try {
            eventJdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(streamId, position);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Exception while reading stream " + streamId));
            verify(logger).warn("Failed to read stream {}", streamId, sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindByStreamIdFromPositionOrderByPositionAscWithPage() throws Exception {
        final UUID streamId = randomUUID();
        final long position = 2L;
        final int pageSize = 10;
        final SQLException sqlException = new SQLException();

        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_BY_STREAM_ID_AND_POSITION_BY_PAGE)).thenThrow(sqlException);

        try {
            eventJdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(streamId, position, pageSize);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Exception while reading stream " + streamId));
            verify(logger).warn("Failed to read stream {}", streamId, sqlException);
        }
    }
}
