package uk.gov.justice.services.eventstore.management.verification.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventLogActiveEventRowCounterTest {

    @InjectMocks
    private EventLogActiveEventRowCounter eventLogActiveEventRowCounter;

    @Test
    public void shouldGetTheNumberOfEventsWhichBelongToAnActiveStream() throws Exception {

        final String query =
                "SELECT count(*) " +
                        "FROM event_log " +
                        "INNER JOIN event_stream " +
                        "ON event_stream.stream_id = event_log.stream_id " +
                        "WHERE event_stream.active = 'True'";

        final int rowCount = 23;

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(rowCount);

        assertThat(eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource), is(rowCount));
    }

    @Test
    public void shouldFailIfAccessingTheDatabaseFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final String query =
                "SELECT count(*) " +
                        "FROM event_log " +
                        "INNER JOIN event_stream " +
                        "ON event_stream.stream_id = event_log.stream_id " +
                        "WHERE event_stream.active = 'True'";

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenThrow(sqlException);

        try {
            eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get count of active events in event_log table"));
        }
    }

    @Test
    public void shouldFailIfNoResultsReturned() throws Exception {

        final String query =
                "SELECT count(*) " +
                        "FROM event_log " +
                        "INNER JOIN event_stream " +
                        "ON event_stream.stream_id = event_log.stream_id " +
                        "WHERE event_stream.active = 'True'";

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(query)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        try {
            eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getMessage(), is("COUNT(*) query returned no results"));
        }
    }
}
