package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.RebuildException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class BatchEventRenumbererTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private EventIdsByBatchProvider eventIdsByBatchProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private BatchEventRenumberer batchEventRenumberer;

    @Test
    public void shouldRenumberABatchOfEvents() throws Exception {

        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final UUID eventId_3 = randomUUID();

        final EventIdBatch eventIdBatch = new EventIdBatch(asList(eventId_1, eventId_2, eventId_3));

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("UPDATE event_log SET event_number = nextval('event_sequence_seq') WHERE id = ?")).thenReturn(preparedStatement);

        batchEventRenumberer.renumberEvents(eventIdBatch);

        final InOrder inOrder = inOrder(
                preparedStatement,
                logger
        );

        inOrder.verify(preparedStatement).setObject(1, eventId_1);
        inOrder.verify(preparedStatement).executeUpdate();
        inOrder.verify(preparedStatement).setObject(1, eventId_2);
        inOrder.verify(preparedStatement).executeUpdate();
        inOrder.verify(preparedStatement).setObject(1, eventId_3);
        inOrder.verify(preparedStatement).executeUpdate();
        inOrder.verify(logger).info("Renumbered 3 events");

        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldThrowExceptionOnSqlExceptionRenumberingEvents() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final UUID eventId_3 = randomUUID();

        final EventIdBatch eventIdBatch = new EventIdBatch(asList(eventId_1, eventId_2, eventId_3));

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(sqlException);

        try {
            batchEventRenumberer.renumberEvents(eventIdBatch);
            fail();
        } catch (final RebuildException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to renumber event_number in event_log table"));
        }
    }

    @Test
    public void shouldGetEventIdsOrderedByCreationDate() throws Exception {

        final EventIdBatch eventIdBatch_1 = mock(EventIdBatch.class);
        final EventIdBatch eventIdBatch_2 = mock(EventIdBatch.class);
        final EventIdBatch eventIdBatch_3 = mock(EventIdBatch.class);

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT id FROM event_log ORDER BY date_created")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(eventIdsByBatchProvider.getNextBatchOfIds(resultSet, 1_000)).thenReturn(eventIdBatch_1, eventIdBatch_2, eventIdBatch_3);

        final List<EventIdBatch> eventIdBatches = batchEventRenumberer.getEventIdsOrderedByCreationDate();

        assertThat(eventIdBatches.size(), is(3));
        assertThat(eventIdBatches.get(0), is(eventIdBatch_1));
        assertThat(eventIdBatches.get(1), is(eventIdBatch_2));
        assertThat(eventIdBatches.get(2), is(eventIdBatch_3));
    }

    @Test
    public void shouldThrowExceptionOnSqlExceptionGettingBatchesOfIds() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT id FROM event_log ORDER BY date_created")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        try {
            batchEventRenumberer.getEventIdsOrderedByCreationDate();
            fail();
        } catch (final RebuildException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get ids from event_log table"));
        }
    }
}
