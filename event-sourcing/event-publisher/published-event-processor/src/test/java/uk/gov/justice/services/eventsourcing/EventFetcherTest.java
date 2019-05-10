package uk.gov.justice.services.eventsourcing;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.EventFetcher;
import uk.gov.justice.services.eventsourcing.publishedevent.EventFetcherRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.EventFetchingException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

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
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private EventFetcherRepository eventFetcherRepository;

    @InjectMocks
    private EventFetcher eventFetcher;

    @Test
    public void shouldRetrieveAnEventFromTheRepository() throws Exception {

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final Optional<Event> event = of(mock(Event.class));

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getEvent(id, connection)).thenReturn(event);

        assertThat(eventFetcher.getEvent(id), is(event));
    }

    @Test
    public void shouldThowExceptionIfGettingAnEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
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
    public void shouldRetrievePublishedEventFromTheRepository() throws Exception {

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final Optional<PublishedEvent> publishedEvent = of(mock(PublishedEvent.class));

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getPublishedEvent(id, connection)).thenReturn(publishedEvent);

        assertThat(eventFetcher.getPublishedEvent(id), is(publishedEvent));
    }

    @Test
    public void shouldThowExceptionIfGettingPublishedEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID id = randomUUID();

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);

        when(eventFetcherRepository.getPublishedEvent(id, connection)).thenThrow(sqlException);

        try {
            eventFetcher.getPublishedEvent(id);
            fail();
        } catch (final EventFetchingException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to get PublishedEvent with id '" + id + "'"));
        }
    }
}
