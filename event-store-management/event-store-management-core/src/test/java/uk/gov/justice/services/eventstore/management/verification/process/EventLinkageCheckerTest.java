package uk.gov.justice.services.eventstore.management.verification.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PROCESSED_EVENT;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PUBLISHED_EVENT;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventLinkageCheckerTest {

    @Mock
    private EventLinkageErrorMessageGenerator eventLinkageErrorMessageGenerator;

    @InjectMocks
    private EventLinkageChecker eventLinkageChecker;

    @Test
    public void shouldReturnSuccessIfAllEventsAreCorrectlyLinked() throws Exception {

        final String sql = "SELECT event_number, previous_event_number FROM published_event ORDER BY event_number";

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
                PUBLISHED_EVENT,
                dataSource);

        assertThat(publishedEventsResults.size(), is(1));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(publishedEventsResults.get(0).getMessage(), is("All 3 events in the published_event table are correctly linked"));
    }

    @Test
    public void shouldReturnErrorIfAnyEventsAreIncorrectlyLinked() throws Exception {

        final String errorMessage = "error message";

        final String sql = "SELECT event_number, previous_event_number FROM published_event ORDER BY event_number";

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

        when(eventLinkageErrorMessageGenerator.generateErrorMessage(3, 4, 2, PUBLISHED_EVENT)).thenReturn(errorMessage);

        final List<VerificationResult> publishedEventsResults = eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                PUBLISHED_EVENT,
                dataSource);

        assertThat(publishedEventsResults.size(), is(1));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(0).getMessage(), is(errorMessage));
    }

    @Test
    public void shouldReturnAllErrors() throws Exception {

        final String errorMessage_1 = "error message 1";
        final String errorMessage_2 = "error message 2";

        final String sql = "SELECT event_number, previous_event_number FROM processed_event ORDER BY event_number";

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

        when(eventLinkageErrorMessageGenerator.generateErrorMessage(2, 3, 1, PROCESSED_EVENT)).thenReturn(errorMessage_1);
        when(eventLinkageErrorMessageGenerator.generateErrorMessage(4, 5, 3, PROCESSED_EVENT)).thenReturn(errorMessage_2);

        final List<VerificationResult> publishedEventsResults = eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                PROCESSED_EVENT,
                dataSource);

        assertThat(publishedEventsResults.size(), is(2));
        assertThat(publishedEventsResults.get(0).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(0).getMessage(), is(errorMessage_1));
        assertThat(publishedEventsResults.get(1).getVerificationResultType(), is(ERROR));
        assertThat(publishedEventsResults.get(1).getMessage(), is(errorMessage_2));
    }

    @Test
    public void shouldThrowExceptionIfDataAccessFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final String sql = "SELECT event_number, previous_event_number FROM published_event ORDER BY event_number";

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
                    PUBLISHED_EVENT,
                    dataSource);
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get event numbers from published_event table"));
        }
    }
}
