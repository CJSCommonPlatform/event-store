package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishedEventRepositoryTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private PublishedEventQueries publishedEventQueries;

    @InjectMocks
    private PublishedEventRepository publishedEventRepository;

    @Test
    public void shouldSaveAPublishedEvent() throws Exception {

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);

        publishedEventRepository.save(publishedEvent);

        verify(publishedEventQueries).insertPublishedEvent(publishedEvent, dataSource);
    }

    @Test
    public void shouldThrowExceptionIfSavingEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(publishedEvent.getId()).thenReturn(eventId);
        doThrow(sqlException).when(publishedEventQueries).insertPublishedEvent(publishedEvent, dataSource);

        final PublishedEventException publishedEventException = assertThrows(
                PublishedEventException.class,
                () -> publishedEventRepository.save(publishedEvent));

        assertThat(publishedEventException.getCause(), is(sqlException));
        assertThat(publishedEventException.getMessage(), is("Unable to insert PublishedEvent with id '019edc7f-a5d5-4143-b026-30eb4d4b14c6'"));
    }

    @Test
    public void shouldGetPublishedEventById() throws Exception {

        final UUID eventId = randomUUID();
        final DataSource eventStoreDataSource = mock(DataSource.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(publishedEventQueries.getPublishedEvent(eventId, eventStoreDataSource)).thenReturn(of(publishedEvent));

        assertThat(publishedEventRepository.getPublishedEvent(eventId), is(of(publishedEvent)));
    }

    @Test
    public void shouldFailIfGettingPublishedEventByIdFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");
        final UUID eventId = UUID.fromString("3fa24506-0738-4987-80ab-ccb20318a195");
        final DataSource eventStoreDataSource = mock(DataSource.class);
        
        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(publishedEventQueries.getPublishedEvent(eventId, eventStoreDataSource)).thenThrow(sqlException);

        final PublishedEventException publishedEventException = assertThrows(
                PublishedEventException.class,
                () -> publishedEventRepository.getPublishedEvent(eventId));

        assertThat(publishedEventException.getCause(), is(sqlException));
        assertThat(publishedEventException.getMessage(), is("Failed to get PublishedEvent with id '3fa24506-0738-4987-80ab-ccb20318a195'"));
    }

}
