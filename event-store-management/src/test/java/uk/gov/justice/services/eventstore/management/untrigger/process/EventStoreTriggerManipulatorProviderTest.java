package uk.gov.justice.services.eventstore.management.untrigger.process;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulator;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulatorFactory;

import javax.inject.Inject;
import javax.sql.DataSource;

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
