package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_ALL_ORDERED_BY_EVENT_NUMBER;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID_AND_POSITION;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_BY_STREAM_ID_AND_POSITION_BY_PAGE;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_FIND_FROM_EVENT_NUMBER_WITH_PAGE;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository.SQL_MAX_EVENT_NUMBER_FROM_EVENT_LOG;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;


// Nb: This is just testing the throwing of exceptions to keep coverage happy. The
// main meat of the class is tested as an integration test: EventJdbcRepositoryIT
@RunWith(MockitoJUnitRunner.class)
public class EventJdbcRepositoryExceptionsTest {

    @Mock
    private EventInsertionStrategy eventInsertionStrategy;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

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
        final DataSource dataSource = mock(DataSource.class);

        when(eventInsertionStrategy.insertStatement()).thenReturn(statement);
        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, statement)).thenThrow(sqlException);
        when(event.getPositionInStream()).thenReturn(5L);
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
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindById() throws Exception {

        final SQLException sqlException = new SQLException();

        final UUID id = fromString("b0c2e210-97ee-4050-a5a1-05f0b77b5eae");
        final String statement = "STATEMENT";
        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(sqlException);

        try {
            eventJdbcRepository.findById(id);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to get event with id 'b0c2e210-97ee-4050-a5a1-05f0b77b5eae'"));
            assertThat(expected.getCause(), is(sqlException));
            verify(logger).error("Failed to get event with id 'b0c2e210-97ee-4050-a5a1-05f0b77b5eae'", sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInGetOrderedStreamOfEvents() throws Exception {

        final SQLException sqlException = new SQLException();

        final String statement = "STATEMENT";
        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(
                dataSource,
                SQL_FIND_ALL_ORDERED_BY_EVENT_NUMBER))
                .thenThrow(sqlException);

        try {
            eventJdbcRepository.findAllOrderedByEventNumber();
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to get stream of events"));
            assertThat(expected.getCause(), is(sqlException));
            verify(logger).error("Failed to get stream of events", sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindByStreamIdOrderByPositionAsc() throws Exception {

        final UUID streamId = randomUUID();
        final SQLException sqlException = new SQLException();

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
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

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
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

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_BY_STREAM_ID_AND_POSITION_BY_PAGE)).thenThrow(sqlException);

        try {
            eventJdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(streamId, position, pageSize);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Exception while reading stream " + streamId));
            verify(logger).error("Failed to read stream {}", streamId, sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInFindAllFromEventNumberUptoPageSize() throws Exception {

        final long eventNumber = 2L;
        final int pageSize = 10;
        final SQLException sqlException = new SQLException();

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_FROM_EVENT_NUMBER_WITH_PAGE)).thenThrow(sqlException);

        try {
            eventJdbcRepository.findAllFromEventNumberUptoPageSize(eventNumber, pageSize);
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Failed to read events from event_log from event number : '2' with page size : '10'"));
            verify(logger).error("Failed to read events from event_log from event number : '2' with page size : '10'", sqlException);
        }
    }

    @Test
    public void shouldLogAndThrowExceptionIfSqlExceptionIsThrownInCountEventsFrom() throws Exception {

        final SQLException sqlException = new SQLException();

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_MAX_EVENT_NUMBER_FROM_EVENT_LOG)).thenThrow(sqlException);

        try {
            eventJdbcRepository.getMaximumEventNumber();
            fail();
        } catch (final JdbcRepositoryException e) {
            assertThat(e.getMessage(), is("Failed to find maximum value of event_number in event_log"));
            verify(logger).error("Failed to find maximum value of event_number in event_log", sqlException);
        }
    }
}
