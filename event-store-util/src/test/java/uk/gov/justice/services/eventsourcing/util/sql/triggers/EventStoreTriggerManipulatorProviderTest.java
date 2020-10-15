package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStoreTriggerManipulatorProviderTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    public DatabaseTriggerManipulatorFactory databaseTriggerManipulatorFactory;

    @InjectMocks
    private EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider;

    @Test
    public void shouldCreateATriggerManipulaterWithTheCorrectEventStoreDataSource() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(databaseTriggerManipulatorFactory.databaseTriggerManipulator(eventStoreDataSource)).thenReturn(databaseTriggerManipulator);

        assertThat(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator(), is(databaseTriggerManipulator));
    }
}
