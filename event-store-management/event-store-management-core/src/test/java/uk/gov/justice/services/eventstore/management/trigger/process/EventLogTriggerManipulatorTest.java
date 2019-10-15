package uk.gov.justice.services.eventstore.management.trigger.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.DatabaseTriggerManipulator;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.TriggerData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventLogTriggerManipulatorTest {

    @Mock
    private EventStoreTriggerManipulatorProvider eventStoreTriggerManipulatorProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Test
    public void shouldAddTriggerToEventLogTable() throws Exception {

        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("update_pre_publish_queue", "event_log")).thenReturn(empty());

        eventLogTriggerManipulator.addTriggerToEventLogTable();

        verify(databaseTriggerManipulator).addInsertTriggerToTable("update_pre_publish_queue", "event_log", "EXECUTE PROCEDURE update_pre_publish_queue()");
        verify(logger).info("Trigger 'update_pre_publish_queue' successfully added to event_log table");
    }

    @Test
    public void shouldNotAddTriggerIfItAlreadyExists() throws Exception {

        final TriggerData triggerData = mock(TriggerData.class);
        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("update_pre_publish_queue", "event_log")).thenReturn(of(triggerData));

        eventLogTriggerManipulator.addTriggerToEventLogTable();

        verify(logger).warn("Trigger 'update_pre_publish_queue' already exists on event_log table");
        verify(databaseTriggerManipulator, never()).addInsertTriggerToTable(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldRemoveTriggerFromEventLogTable() throws Exception {

        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);
        final TriggerData triggerData = mock(TriggerData.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("update_pre_publish_queue", "event_log")).thenReturn(of(triggerData));

        eventLogTriggerManipulator.removeTriggerFromEventLogTable();

        verify(databaseTriggerManipulator).removeTriggerFromTable("update_pre_publish_queue", "event_log");
        verify(logger).info("Removed trigger 'update_pre_publish_queue' from event_log table");
    }

    @Test
    public void shouldNotRemoveTriggerIfItNoTriggerExists() throws Exception {

        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("update_pre_publish_queue", "event_log")).thenReturn(empty());

        eventLogTriggerManipulator.removeTriggerFromEventLogTable();

        verify(logger).warn("No trigger named 'update_pre_publish_queue' found on event_log table");
        verify(databaseTriggerManipulator, never()).removeTriggerFromTable(anyString(), anyString());
    }
}
