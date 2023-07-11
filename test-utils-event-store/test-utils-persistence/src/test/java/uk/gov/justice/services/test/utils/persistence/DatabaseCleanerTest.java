package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class DatabaseCleanerTest {
    private static final String SQL_PATTERN = "TRUNCATE TABLE %s CASCADE";

    private final TestJdbcConnectionProvider testJdbcConnectionProvider = mock(TestJdbcConnectionProvider.class);

    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner(testJdbcConnectionProvider);

    @Test
    public void shouldCleanSomeViewStoreTables() throws Exception {

        final String table_1 = "table_1";
        final String table_2 = "table_2";

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, table_1))).thenReturn(preparedStatement_1);
        when(connection.prepareStatement(format(SQL_PATTERN, table_2))).thenReturn(preparedStatement_2);

        databaseCleaner.cleanViewStoreTables(contextName, table_1, table_2);

        verify(preparedStatement_1).executeUpdate();
        verify(preparedStatement_2).executeUpdate();

        verify(connection).close();
        verify(preparedStatement_1).close();
        verify(preparedStatement_2).close();
    }

    @Test
    public void shouldCleanTheEventStoreTables() throws Exception {

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, "event_log"))).thenReturn(preparedStatement);
        when(connection.prepareStatement(format(SQL_PATTERN, "event_stream"))).thenReturn(preparedStatement);
        when(connection.prepareStatement(format(SQL_PATTERN, "pre_publish_queue"))).thenReturn(preparedStatement);
        when(connection.prepareStatement(format(SQL_PATTERN, "publish_queue"))).thenReturn(preparedStatement);
        when(connection.prepareStatement(format(SQL_PATTERN, "published_event"))).thenReturn(preparedStatement);

        databaseCleaner.cleanEventStoreTables(contextName);

        verify(preparedStatement, times(5)).executeUpdate();
        verify(connection).close();
        verify(preparedStatement, times(5)).close();
    }

    @Test
    public void shouldCleanTheSystemTables() throws Exception {

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getSystemConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, "stored_command"))).thenReturn(preparedStatement);

        databaseCleaner.cleanSystemTables(contextName);

        verify(preparedStatement, times(1)).executeUpdate();
        verify(connection).close();
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void shouldCleanTheStreamBufferTable() throws Exception {

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);

        databaseCleaner.cleanStreamBufferTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCleanTheStreamStatusTable() throws Exception {

        final String tableName = "stream_status";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);

        databaseCleaner.cleanStreamStatusTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCleanTheProcessedEventTable() throws Exception {

        final String tableName = "processed_event";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);

        databaseCleaner.cleanProcessedEventTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCleaningAViewStoreTableFails() throws Exception {

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenThrow(sqlException);

        try {
            databaseCleaner.cleanStreamBufferTable(contextName);
            fail();
        } catch (DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to delete content from table " + tableName));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCleaningTheEventStoreTablesFails() throws Exception {

        final String tableName = "event_log";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenThrow(sqlException);

        try {
            databaseCleaner.cleanEventLogTable(contextName);
            fail();
        } catch (DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to delete content from table " + tableName));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADatAccessExceptionIfClosingTheViewStoreConnectionFails() throws Exception {

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);
        doThrow(sqlException).when(preparedStatement).close();

        try {
            databaseCleaner.cleanStreamBufferTable(contextName);
            fail();
        } catch (Exception expected) {
            assertThat(expected.getCause(), is(sqlException));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADatAccessExceptionIfClosingTheSystemConnectionFails() throws Exception {

        final String tableName = "stored_command";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getSystemConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);
        doThrow(sqlException).when(preparedStatement).close();

        try {
            databaseCleaner.cleanSystemTables(contextName);
            fail();
        } catch (final DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADatAccessExceptionIfClosingTheEventStoreConnectionFails() throws Exception {

        final String tableName = "event_log";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, tableName))).thenReturn(preparedStatement);
        doThrow(sqlException).when(preparedStatement).close();

        try {
            databaseCleaner.cleanEventLogTable(contextName);
            fail();
        } catch (Exception expected) {
            assertThat(expected.getCause(), is(sqlException));
        }

        verify(connection).close();
    }

    @Test
    public void shouldCleanSomeEventStoreTables() throws Exception {

        final String table_1 = "table_1";
        final String table_2 = "table_2";

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement(format(SQL_PATTERN, table_1))).thenReturn(preparedStatement_1);
        when(connection.prepareStatement(format(SQL_PATTERN, table_2))).thenReturn(preparedStatement_2);

        databaseCleaner.cleanEventStoreTables(contextName, table_1, table_2);

        verify(preparedStatement_1).executeUpdate();
        verify(preparedStatement_2).executeUpdate();

        verify(connection).close();
        verify(preparedStatement_1).close();
        verify(preparedStatement_2).close();
    }
}
