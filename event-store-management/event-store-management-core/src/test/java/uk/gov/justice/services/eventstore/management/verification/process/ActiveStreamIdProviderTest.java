package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStreamIdProviderTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @InjectMocks
    private ActiveStreamIdProvider activeStreamIdProvider;

    @Test
    public void shouldGetAllActiveStreamIds() throws Exception {

        final UUID activeStreamId_1 = randomUUID();
        final UUID activeStreamId_2 = randomUUID();
        final UUID activeStreamId_3 = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT stream_id from event_stream where active = 'true'")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getObject("stream_id")).thenReturn(activeStreamId_1, activeStreamId_2, activeStreamId_3);

        final Set<UUID> allActiveStreamIds = activeStreamIdProvider.getAllActiveStreamIds();

        assertThat(allActiveStreamIds.size(), is(3));
        assertThat(allActiveStreamIds, hasItem(activeStreamId_1));
        assertThat(allActiveStreamIds, hasItem(activeStreamId_2));
        assertThat(allActiveStreamIds, hasItem(activeStreamId_3));

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    public void shouldThrowExceptionIfGettingActiveStreamIdsFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT stream_id from event_stream where active = 'true'")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(sqlException);

        try {
            activeStreamIdProvider.getAllActiveStreamIds();
            fail();
        } catch (final CatchupVerificationException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get the list of active stream ids from stream_status"));
        }

        verify(preparedStatement).close();
        verify(connection).close();
    }
}
