package uk.gov.justice.services.eventstore.management.untrigger.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
        when(databaseTriggerManipulator.findTriggerOnTable("queue_publish_event", "event_log")).thenReturn(empty());

        eventLogTriggerManipulator.addTriggerToEventLogTable();

        verify(databaseTriggerManipulator).addInsertTriggerToTable("queue_publish_event", "event_log", "EXECUTE PROCEDURE update_publish_queue()");
        verify(logger).info("Trigger 'queue_publish_event' successfully added to event_log table");
    }

    @Test
    public void shouldNotAddTriggerIfItAlreadyExists() throws Exception {

        final TriggerData triggerData = mock(TriggerData.class);
        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("queue_publish_event", "event_log")).thenReturn(of(triggerData));

        eventLogTriggerManipulator.addTriggerToEventLogTable();

        verify(logger).warn("Trigger 'queue_publish_event' already exists on event_log table");
        verify(databaseTriggerManipulator, never()).addInsertTriggerToTable(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldRemoveTriggerFromEventLogTable() throws Exception {

        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);
        final TriggerData triggerData = mock(TriggerData.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("queue_publish_event", "event_log")).thenReturn(of(triggerData));

        eventLogTriggerManipulator.removeTriggerFromEventLogTable();

        verify(databaseTriggerManipulator).removeTriggerFromTable("queue_publish_event", "event_log");
        verify(logger).info("Removed trigger 'queue_publish_event' from event_log table");
    }

    @Test
    public void shouldNotRemoveTriggerIfItNoTriggerExists() throws Exception {

        final DatabaseTriggerManipulator databaseTriggerManipulator = mock(DatabaseTriggerManipulator.class);

        when(eventStoreTriggerManipulatorProvider.getDatabaseTriggerManipulator()).thenReturn(databaseTriggerManipulator);
        when(databaseTriggerManipulator.findTriggerOnTable("queue_publish_event", "event_log")).thenReturn(empty());

        eventLogTriggerManipulator.removeTriggerFromEventLogTable();

        verify(logger).warn("No trigger named 'queue_publish_event' found on event_log table");
        verify(databaseTriggerManipulator, never()).removeTriggerFromTable(anyString(), anyString());
    }
}
