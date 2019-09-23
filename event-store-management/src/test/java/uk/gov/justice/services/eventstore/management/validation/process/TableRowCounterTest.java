package uk.gov.justice.services.eventstore.management.validation.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TableRowCounterTest {

    @InjectMocks
    private TableRowCounter tableRowCounter;

    @Test
    public void shouldCountTheRowsInTheSpecifiedTable() throws Exception {

        final String tableName = "published_event";
        final int rowCount = 2347;

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT count(*) FROM published_event")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(rowCount);

        assertThat(tableRowCounter.countRowsIn(tableName, eventStoreDataSource), is(rowCount));
    }

    @Test
    public void shouldFailIfCountingTheRowsInTheSpecifiedTableFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final String tableName = "published_event";

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT count(*) FROM published_event")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenThrow(sqlException);

        try {
            tableRowCounter.countRowsIn(tableName, eventStoreDataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to count rows in published_event table"));
        }
    }

    @Test
    public void shouldFailIfNoResultsReturned() throws Exception {

        final String tableName = "published_event";

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT count(*) FROM published_event")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        try {
            tableRowCounter.countRowsIn(tableName, eventStoreDataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getMessage(), is("Counting rows in published_event table did not return any results"));
        }
    }
}
