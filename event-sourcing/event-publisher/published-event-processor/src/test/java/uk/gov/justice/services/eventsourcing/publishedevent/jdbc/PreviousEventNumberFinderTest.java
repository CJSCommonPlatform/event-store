package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
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
public class PreviousEventNumberFinderTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private PrePublishRepository prePublishRepository;

    @InjectMocks
    private PreviousEventNumberFinder previousEventNumberFinder;


    @Test
    public void shouldGetThePreviousEventNumber() throws Exception {

        final long eventNumber = 23L;
        final long previousEventNumber = 22L;
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);

        when(prePublishRepository.getPreviousEventNumber(eventNumber, dataSource)).thenReturn(previousEventNumber);

        assertThat(previousEventNumberFinder.getPreviousEventNumber(eventId, eventNumber), is(previousEventNumber));
    }

    @Test
    public void shouldThrowExceptionIfGettingThePreviousEventNumberFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final long eventNumber = 23L;
        final UUID eventId = fromString("019edc7f-a5d5-4143-b026-30eb4d4b14c6");

        final DataSource dataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, dataSource)).thenThrow(sqlException);

        try {
            previousEventNumberFinder.getPreviousEventNumber(eventId, eventNumber);
            fail();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to get previous event number for event with id '019edc7f-a5d5-4143-b026-30eb4d4b14c6'"));
        }
    }
}
