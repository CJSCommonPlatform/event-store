package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.INSERT_INTO_PUBLISHED_EVENT_SQL;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.util.io.Closer;
import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BatchedPublishedEventInserterTest {

    @Mock
    private Closer closer;

    @InjectMocks
    private BatchedPublishedEventInserter batchedPublishedEventInserter;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldAddToBatchForInsert() throws Exception {

        final PublishedEvent publishedEvent = aPublishedEvent();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenReturn(preparedStatement);

        batchedPublishedEventInserter.prepareForInserts(eventStoreDataSource);
        batchedPublishedEventInserter.addToBatch(publishedEvent);

        final InOrder inOrder = inOrder(preparedStatement);

        inOrder.verify(preparedStatement).setObject(1, publishedEvent.getId());
        inOrder.verify(preparedStatement).setObject(2, publishedEvent.getStreamId());
        inOrder.verify(preparedStatement).setLong(3, publishedEvent.getPositionInStream());
        inOrder.verify(preparedStatement).setString(4, publishedEvent.getName());
        inOrder.verify(preparedStatement).setString(5, publishedEvent.getPayload());
        inOrder.verify(preparedStatement).setString(6, publishedEvent.getMetadata());
        inOrder.verify(preparedStatement).setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
        inOrder.verify(preparedStatement).setLong(8, publishedEvent.getEventNumber().orElse(null));
        inOrder.verify(preparedStatement).setLong(9, publishedEvent.getPreviousEventNumber());

        inOrder.verify(preparedStatement).addBatch();

        verify(preparedStatement, never()).executeBatch();

        batchedPublishedEventInserter.insertBatch();

        verify(preparedStatement).executeBatch();
    }

    @Test
    public void shouldCloseStatementAndConnectionOnClose() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenReturn(preparedStatement);

        batchedPublishedEventInserter.prepareForInserts(eventStoreDataSource);

        batchedPublishedEventInserter.close();

        final InOrder inOrder = inOrder(closer);

        inOrder.verify(closer).closeQuietly(preparedStatement);
        inOrder.verify(closer).closeQuietly(connection);
    }

    @Test
    public void shouldThrowExceptionIfPreparingForInsertsFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenThrow(sqlException);

        try {
            batchedPublishedEventInserter.prepareForInserts(eventStoreDataSource);
            fail();
        } catch (final DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to prepare statement for batch insert of PublishedEvents"));
        }
    }

    @Test
    public void shouldThrowExceptionIfAddingToBatchFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");

        final PublishedEvent publishedEvent = aPublishedEvent();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenReturn(preparedStatement);

        batchedPublishedEventInserter.prepareForInserts(eventStoreDataSource);

        doThrow(sqlException).when(preparedStatement).addBatch();

        try {
            batchedPublishedEventInserter.addToBatch(publishedEvent);
            fail();
        } catch (final DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to add PublishedEvent to batch"));
        }
    }

    @Test
    public void shouldThrowExceptionIfExecutingBatchFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenReturn(preparedStatement);

        batchedPublishedEventInserter.prepareForInserts(eventStoreDataSource);

        doThrow(sqlException).when(preparedStatement).executeBatch();

        try {
            batchedPublishedEventInserter.insertBatch();
            fail();
        } catch (final DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to insert batch of PublishedEvents"));
        }
    }

    private PublishedEvent aPublishedEvent() {
        return new PublishedEvent(
                randomUUID(),
                randomUUID(),
                23L,
                "event-name",
                "metadata",
                "payload",
                new UtcClock().now(),
                234L,
                233L
        );
    }
}
