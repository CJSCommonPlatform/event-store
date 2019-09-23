package uk.gov.justice.services.eventstore.management.validation.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventLinkageCheckerTest {

    @InjectMocks
    private EventLinkageChecker eventLinkageChecker;

    @Test
    public void shouldReturnSuccessIfAllEventsAreCorrectlyLinked() throws Exception {

        final String tableName = "some_table";

        final String sql = "SELECT event_number, previous_event_number FROM some_table ORDER BY event_number";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);

        when(resultSet.getInt("previous_event_number")).thenReturn(0, 1, 2);
        when(resultSet.getInt("event_number")).thenReturn(1, 2, 3);

        final List<VerificationResult> publishedEventsResults = eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                tableName,
                dataSource);

        assertThat(publishedEventsResults.size(), is(1));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(publishedEventsResults.get(0).getMessage(), is("All 3 events in the some_table table are correctly linked"));
    }

    @Test
    public void shouldReturnErrorIfAnyEventsAreIncorrectlyLinked() throws Exception {

        final String tableName = "some_table";

        final String sql = "SELECT event_number, previous_event_number FROM some_table ORDER BY event_number";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);

        when(resultSet.getInt("previous_event_number")).thenReturn(0, 1, 3);
        when(resultSet.getInt("event_number")).thenReturn(1, 2, 4);

        final List<VerificationResult> publishedEventsResults = eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                tableName,
                dataSource);

        assertThat(publishedEventsResults.size(), is(1));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(0).getMessage(), is("Events incorrectly linked in some_table table. Event with event number 4 is linked to previous event number 3 whereas it should be 2"));
    }

    @Test
    public void shouldReturnAllErrors() throws Exception {

        final String tableName = "some_table";

        final String sql = "SELECT event_number, previous_event_number FROM some_table ORDER BY event_number";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);

        when(resultSet.getInt("previous_event_number")).thenReturn(0, 2, 4);
        when(resultSet.getInt("event_number")).thenReturn(1, 3, 5);

        final List<VerificationResult> publishedEventsResults = eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                tableName,
                dataSource);

        assertThat(publishedEventsResults.size(), is(2));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(0).getMessage(), is("Events incorrectly linked in some_table table. Event with event number 3 is linked to previous event number 2 whereas it should be 1"));
        assertThat(publishedEventsResults.get(1).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(1).getMessage(), is("Events incorrectly linked in some_table table. Event with event number 5 is linked to previous event number 4 whereas it should be 3"));
    }

    @Test
    public void shouldThrowExceptionIfDataAccessFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");
        final String tableName = "some_table";

        final String sql = "SELECT event_number, previous_event_number FROM some_table ORDER BY event_number";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(sqlException);

        try {
            eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                    tableName,
                    dataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get event numbers from some_table table"));
        }
    }
}
