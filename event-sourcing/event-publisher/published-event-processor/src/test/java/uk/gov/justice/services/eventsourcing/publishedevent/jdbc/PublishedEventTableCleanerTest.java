package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventTableCleanerTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private DatabaseTableTruncator databaseTableTruncator;

    @InjectMocks
    private PublishedEventTableCleaner publishedEventTableCleaner;

    @Test
    public void shouldTruncateThePublishedEventTable() throws Exception {

        final DataSource defaultDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(defaultDataSource);

        publishedEventTableCleaner.deleteAll();

        verify(databaseTableTruncator).truncate("published_event", defaultDataSource);
    }

    @Test
    public void shouldThrowExceptionIfTruncatingPublishedEventFails() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final DataSource defaultDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(defaultDataSource);
        doThrow(sqlException).when(databaseTableTruncator).truncate("published_event", defaultDataSource);

        try {
            publishedEventTableCleaner.deleteAll();
            fail();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to truncate table 'published_event'"));
        }


    }
}
