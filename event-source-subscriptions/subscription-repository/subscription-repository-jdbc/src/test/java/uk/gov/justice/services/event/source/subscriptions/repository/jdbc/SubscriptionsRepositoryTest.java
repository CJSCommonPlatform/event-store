package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionsRepositoryTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Mock
    SubscriptionsJdbc subscriptionsJdbc;

    @InjectMocks
    private SubscriptionsRepository subscriptionsRepository;

    @Test
    public void shouldInsertOrUpdateCurrentEventNumber() throws Exception {

        final long eventNumber = 928374;
        final String subscriptionName = "a subscription";

        final Connection connection = mock(Connection.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource().getConnection()).thenReturn(connection);

        subscriptionsRepository.insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName);

        verify(subscriptionsJdbc).insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName, connection);

        verify(connection).close();
    }

    @Test
    public void shouldThrowExceptionIfInsertOrUpdateCurrentEventNumberFails() throws Exception {

        final long eventNumber = 928374;
        final String subscriptionName = "a subscription";
        final SQLException sqlException = new SQLException();

        final Connection connection = mock(Connection.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource().getConnection()).thenReturn(connection);
        doThrow(sqlException).when(subscriptionsJdbc).insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName, connection);

        try {
            subscriptionsRepository.insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName);
            fail();
        } catch (final SubscriptionRepositoryException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to update current_event_number to 928374 in subscriptions table."));
        }

        verify(connection).close();
    }

    @Test
    public void shouldGetTheCurrentEventNumber() throws Exception {
        final long eventNumber = 928374;
        final String subscriptionName = "a subscription";

        final Connection connection = mock(Connection.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource().getConnection()).thenReturn(connection);
        when(subscriptionsJdbc.readCurrentEventNumber(subscriptionName, connection)).thenReturn(of(eventNumber));

        assertThat(subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscriptionName), is(eventNumber));

        verify(connection).close();
    }

    @Test
    public void shouldInitialiseToZeroIfCurrentEventNumberDoesNotExist() throws Exception {

        final long initialEventNumber = 0;
        final String subscriptionName = "a subscription";

        final Connection connection = mock(Connection.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource().getConnection()).thenReturn(connection);
        when(subscriptionsJdbc.readCurrentEventNumber(subscriptionName, connection)).thenReturn(empty(), of(initialEventNumber));

        assertThat(subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscriptionName), is(initialEventNumber));

        verify(subscriptionsJdbc).insertOrUpdateCurrentEventNumber(
                initialEventNumber,
                subscriptionName,
                connection);
        verify(connection).close();
    }

    @Test
    public void shouldThrowExceptionIfGettingCurrentEventNumberFails() throws Exception {

        final String subscriptionName = "a subscription";
        final SQLException sqlException = new SQLException();

        final Connection connection = mock(Connection.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource().getConnection()).thenReturn(connection);
        when(subscriptionsJdbc.readCurrentEventNumber(subscriptionName, connection)).thenThrow(sqlException);

        try {
            subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscriptionName);
            fail();
        } catch (final SubscriptionRepositoryException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get current_event_number from subscriptions table."));
        }

        verify(connection).close();
    }
}
