package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventRepositoryTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private PublishedEventQueries publishedEventQueries;

    @Mock
    private PrePublishRepository prePublishRepository;

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

        try {
            publishedEventRepository.save(publishedEvent);
            fail();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to insert PublishedEvent with id '019edc7f-a5d5-4143-b026-30eb4d4b14c6'"));
        }
    }
}
