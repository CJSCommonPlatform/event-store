package uk.gov.justice.services.eventsourcing;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventFetcherTest {

    @Mock
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Mock
    EventFetcherRepository eventFetcherRepository;

    @InjectMocks
    private EventFetcher eventFetcher;

    @Test
    public void shouldRetrieveAnEventFromTheRepository() throws Exception {

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final Optional<Event> event = of(mock(Event.class));

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getEvent(id, connection)).thenReturn(event);

        assertThat(eventFetcher.getEvent(id), is(event));
    }

    @Test
    public void shouldThrowExceptionIfGettingAnEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getEvent(id, connection)).thenThrow(sqlException);

        try {
            eventFetcher.getEvent(id);
            fail();
        } catch (final EventFetchingException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get Event with id '" + id + "'"));
        }
    }

    @Test
    public void shouldRetrieveLinkedEventFromTheRepository() throws Exception {

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final Optional<LinkedEvent> linkedEvent = of(mock(LinkedEvent.class));

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getLinkedEvent(id, connection)).thenReturn(linkedEvent);

        assertThat(eventFetcher.getLinkedEvent(id), is(linkedEvent));
    }

    @Test
    public void shouldThrowExceptionIfGettingLinkedEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getLinkedEvent(id, connection)).thenThrow(sqlException);

        try {
            eventFetcher.getLinkedEvent(id);
            fail();
        } catch (final EventFetchingException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get LinkedEvent with id '" + id + "'"));
        }
    }
}
