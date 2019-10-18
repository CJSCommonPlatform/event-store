package uk.gov.justice.services.eventstore.management.validation.process.verifiers;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.WARNING;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.validation.process.ActiveStreamIdProvider;
import uk.gov.justice.services.eventstore.management.validation.process.AllEventsInStreamsVerifier;
import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationException;
import uk.gov.justice.services.eventstore.management.validation.process.VerificationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class AllEventsInStreamsVerifierTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private ActiveStreamIdProvider activeStreamIdProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private AllEventsInStreamsVerifier allEventsInStreamsVerifier;

    @Test
    public void shouldReturnSuccessIfAllActiveStreamsHaveAtLeastOneEventInTheEventLogTable() throws Exception {

        final UUID activeStreamId_1 = randomUUID();
        final UUID activeStreamId_2 = randomUUID();
        final UUID activeStreamId_3 = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(activeStreamIdProvider.getAllActiveStreamIds()).thenReturn(newHashSet(activeStreamId_1, activeStreamId_2, activeStreamId_3));

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT DISTINCT stream_id from event_log")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getObject("stream_id")).thenReturn(activeStreamId_1, activeStreamId_2, activeStreamId_3);

        final List<VerificationResult> verificationResult = allEventsInStreamsVerifier.verify();

        assertThat(verificationResult.size(), is(1));

        assertThat(verificationResult.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(verificationResult.get(0).getMessage(), is("All streams have at least one event"));

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    public void shouldReturnWarningIfAnyActiveStreamsDoNotHaveAtLeastOneEventInTheEventLogTable() throws Exception {

        final UUID activeStreamId_1 = fromString("84f05b66-df81-4196-b30d-8d47daa69b6b");
        final UUID activeStreamId_2 = fromString("ee06dc8e-0fe8-4355-8b3b-b57e65153847");
        final UUID activeStreamId_3 = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(activeStreamIdProvider.getAllActiveStreamIds()).thenReturn(newHashSet(activeStreamId_1, activeStreamId_2, activeStreamId_3));

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT DISTINCT stream_id from event_log")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getObject("stream_id")).thenReturn(activeStreamId_3);

        final List<VerificationResult> verificationResult = allEventsInStreamsVerifier.verify();

        assertThat(verificationResult.size(), is(1));

        assertThat(verificationResult.get(0).getVerificationResultType(), is(WARNING));
        assertThat(verificationResult.get(0).getMessage(), is("The following 2 streams in the stream_status table have no events: [ee06dc8e-0fe8-4355-8b3b-b57e65153847, 84f05b66-df81-4196-b30d-8d47daa69b6b]"));

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    public void shouldThrowExceptionIfGettingStreamIdsFromTheEventLogTableFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(activeStreamIdProvider.getAllActiveStreamIds()).thenReturn(newHashSet());

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT DISTINCT stream_id from event_log")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        try {
            allEventsInStreamsVerifier.verify();
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get the stream ids from event_log"));
        }

        verify(preparedStatement).close();
        verify(connection).close();
    }
}
