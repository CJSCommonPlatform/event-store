package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventInserter;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.Connection;
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
    private PublishedEventInserter publishedEventInserter;

    @Mock
    private PrePublishRepository prePublishRepository;

    @InjectMocks
    private PublishedEventRepository publishedEventRepository;

    @Test
    public void shouldSaveAPublishedEvent() throws Exception {

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        publishedEventRepository.save(publishedEvent);

        verify(publishedEventInserter).insertPublishedEvent(publishedEvent, connection);
    }

    @Test
    public void shouldThrowExceptionIfSavingEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(publishedEvent.getId()).thenReturn(eventId);
        doThrow(sqlException).when(publishedEventInserter).insertPublishedEvent(publishedEvent, connection);

        try {
            publishedEventRepository.save(publishedEvent);
            fail();
        } catch (final PublishedEventSQLException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to insert PublishedEvent with id '019edc7f-a5d5-4143-b026-30eb4d4b14c6'"));
        }
    }

    @Test
    public void shouldGetThePreviousEventNumber() throws Exception {

        final long eventNumber = 23L;
        final long previousEventNumber = 22L;
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenReturn(previousEventNumber);

        assertThat(publishedEventRepository.getPreviousEventNumber(eventId, eventNumber), is(previousEventNumber));
    }

    @Test
    public void shouldThrowExceptionIfGettingThePreviousEventNumberFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final long eventNumber = 23L;
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenThrow(sqlException);

        try {
            publishedEventRepository.getPreviousEventNumber(eventId, eventNumber);
            fail();
        } catch (final PublishedEventSQLException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to get previous event number for event with id '019edc7f-a5d5-4143-b026-30eb4d4b14c6'"));
        }
    }
}
