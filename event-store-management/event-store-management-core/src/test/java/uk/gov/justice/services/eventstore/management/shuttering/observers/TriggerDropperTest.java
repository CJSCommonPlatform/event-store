package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.TriggerManipulationFailedException;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class TriggerDropperTest {

    @Mock
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Mock
    private Logger logger;

    @InjectMocks
    private TriggerDropper triggerDropper;

    @Test
    public void shouldBothSuspendAndUnsuspend() throws Exception {

        assertThat(triggerDropper.shouldSuspend(), is(true));
        assertThat(triggerDropper.shouldUnsuspend(), is(true));
    }

    @Test
    public void shouldRemoveTriggerFromEventLogTableWhenSuspending() throws Exception {

        final UUID commandId = randomUUID();
        final SuspendCommand suspendCommand = new SuspendCommand();

        final SuspensionResult suspensionResult = triggerDropper.suspend(commandId, suspendCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getSuspendableName(), is("TriggerDropper"));
        assertThat(suspensionResult.getSystemCommand(), is(suspendCommand));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getMessage(), is("Trigger successfully removed from event_log table"));
        assertThat(suspensionResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(eventLogTriggerManipulator, logger);

        inOrder.verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
        inOrder.verify(logger).info("Trigger successfully removed from event_log table");
    }

    @Test
    public void shouldReturnUnsuccessfulResultIfRemovingTheTriggerFails() throws Exception {

        final UUID commandId = randomUUID();
        final SuspendCommand suspendCommand = new SuspendCommand();

        final TriggerManipulationFailedException triggerManipulationFailedException = new TriggerManipulationFailedException("Ooops");

        doThrow(triggerManipulationFailedException).when(eventLogTriggerManipulator).removeTriggerFromEventLogTable();

        final SuspensionResult suspensionResult = triggerDropper.suspend(commandId, suspendCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getSuspendableName(), is("TriggerDropper"));
        assertThat(suspensionResult.getSystemCommand(), is(suspendCommand));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(suspensionResult.getMessage(), is("Failed to remove trigger from event_log table: TriggerManipulationFailedException:Ooops"));
        assertThat(suspensionResult.getException(), is(of(triggerManipulationFailedException)));

        final InOrder inOrder = inOrder(eventLogTriggerManipulator, logger);

        inOrder.verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
        inOrder.verify(logger).error("Failed to remove trigger from event_log table: TriggerManipulationFailedException:Ooops", triggerManipulationFailedException);
    }

    @Test
    public void shouldAddTriggerToEventLogTableWhenUnsuspending() throws Exception {

        final UUID commandId = randomUUID();
        final UnsuspendCommand unsuspendCommand = new UnsuspendCommand();

        final SuspensionResult suspensionResult = triggerDropper.unsuspend(commandId, unsuspendCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getSuspendableName(), is("TriggerDropper"));
        assertThat(suspensionResult.getSystemCommand(), is(unsuspendCommand));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getMessage(), is("Trigger successfully added to event_log table"));
        assertThat(suspensionResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(eventLogTriggerManipulator, logger);

        inOrder.verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
        inOrder.verify(logger).info("Trigger successfully added to event_log table");
    }

    @Test
    public void shouldReturnUnsuccessfulResultIfAddingTheTriggerFails() throws Exception {

        final UUID commandId = randomUUID();
        final UnsuspendCommand unsuspendCommand = new UnsuspendCommand();
        final TriggerManipulationFailedException triggerManipulationFailedException = new TriggerManipulationFailedException("Ooops");

        doThrow(triggerManipulationFailedException).when(eventLogTriggerManipulator).addTriggerToEventLogTable();

        final SuspensionResult suspensionResult = triggerDropper.unsuspend(commandId, unsuspendCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getSuspendableName(), is("TriggerDropper"));
        assertThat(suspensionResult.getSystemCommand(), is(unsuspendCommand));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(suspensionResult.getMessage(), is("Failed to add trigger to event_log table: TriggerManipulationFailedException:Ooops"));
        assertThat(suspensionResult.getException(), is(of(triggerManipulationFailedException)));

        final InOrder inOrder = inOrder(eventLogTriggerManipulator, logger);

        inOrder.verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
        inOrder.verify(logger).error("Failed to add trigger to event_log table: TriggerManipulationFailedException:Ooops", triggerManipulationFailedException);
    }
}
